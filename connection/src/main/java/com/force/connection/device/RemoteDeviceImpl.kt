package com.force.connection.device

import androidx.compose.runtime.mutableStateMapOf
import com.force.confb.pmodel.BooleanParameter
import com.force.confb.pmodel.FloatParameter
import com.force.confb.pmodel.HandshakeResponse
import com.force.confb.pmodel.IntParameter
import com.force.confb.pmodel.Message
import com.force.confb.pmodel.ParameterInfo
import com.force.confb.pmodel.SetBooleanParameter
import com.force.confb.pmodel.SetFloatParameter
import com.force.confb.pmodel.SetIntParameter
import com.force.confb.pmodel.SetStringParameter
import com.force.confb.pmodel.StringParameter
import com.force.connection.CONN_TAG
import com.force.connection.ConnectionDefaults.log
import com.force.connection.ConnectionDefaults.logAnalytics
import com.force.connection.connection.DeviceConnection
import com.force.model.Device
import com.force.model.DeviceParameter
import com.google.protobuf.ByteString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.nio.charset.Charset
import kotlin.reflect.KClass

@OptIn(ExperimentalCoroutinesApi::class)
class RemoteDeviceImpl(
    private val scope: CoroutineScope,
    private val connection: DeviceConnection
) : RemoteDevice {
    override val name = connection.info.map { info -> info.name }
        .stateIn(scope, SharingStarted.Eagerly, null)

    override val address = connection.info.map { info -> info.address }
        .stateIn(scope, SharingStarted.Eagerly, null)

    private val _state = MutableStateFlow<RemoteDevice.State>(RemoteDevice.State.Connecting)

    override val state = merge(
        _state,
        connection.state
            .filter { it !is DeviceConnection.State.Connected }
            .map {
                when (it) {
                    is DeviceConnection.State.Connecting -> RemoteDevice.State.Connecting
                    is DeviceConnection.State.Disconnected -> RemoteDevice.State.Disconnected
                    is DeviceConnection.State.Error -> RemoteDevice.State.Error(it.error)
                    is DeviceConnection.State.Connected -> throw IllegalStateException(
                        "Unexpected state: DeviceConnection.State.Connected should not be emitted here"
                    )
                }
            }
    )

    private val _parameters = mutableStateMapOf<Int, DeviceParameter<*>>()

    private val _events = MutableSharedFlow<RemoteDevice.Event>(
        replay = 0,
        extraBufferCapacity = 32,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override val events = _events

    override val parameters = _parameters

    private val debounceJobs = mutableMapOf<Int, Job>()

    init {
        scope.launch(Dispatchers.IO) {
            connection.dataObjects
                .filterIsInstance<ParameterInfo>()
                .collect { parameterInfo ->
                    _parameters[parameterInfo.id] = _parameters[parameterInfo.id].let { ep ->
                        when (parameterInfo.type) {
                            0 -> DeviceParameter(
                                parameterInfo.id,
                                ep?.value ?: 0,
                                ep?.name ?: parameterInfo.name.toStringUtf8(),
                                ep?.description ?: parameterInfo.description.toStringUtf8(),
                                ep?.minValue ?: parameterInfo.minValue.toInt(),
                                ep?.maxValue ?: parameterInfo.maxValue.toInt(),
                                ep?.editable ?: parameterInfo.editable,
                                ep?.editable ?: false
                            )

                            1 -> DeviceParameter(
                                parameterInfo.id,
                                ep?.value ?: 0f,
                                ep?.name ?: parameterInfo.name.toStringUtf8(),
                                ep?.description ?: parameterInfo.description.toStringUtf8(),
                                ep?.minValue ?: parameterInfo.minValue,
                                ep?.maxValue ?: parameterInfo.maxValue,
                                ep?.editable ?: parameterInfo.editable,
                                ep?.editable ?: false
                            )

                            2 -> DeviceParameter(
                                parameterInfo.id,
                                ep?.value ?: "",
                                ep?.name ?: parameterInfo.name.toStringUtf8(),
                                ep?.description ?: parameterInfo.description.toStringUtf8(),
                                editable = ep?.editable ?: parameterInfo.editable,
                                changeRequestSend = ep?.editable ?: false
                            )

                            else -> DeviceParameter(
                                parameterInfo.id,
                                ep?.value ?: false,
                                ep?.name ?: parameterInfo.name.toStringUtf8(),
                                ep?.description ?: parameterInfo.description.toStringUtf8(),
                                editable = ep?.editable ?: parameterInfo.editable,
                                changeRequestSend = ep?.editable ?: false
                            )

                        }
                    }
                }
        }

        scope.launch(Dispatchers.IO) {
            connection.dataObjects
                .collect {
                    (it as? HandshakeResponse)?.let {
                        Device(
                            name.value ?: "Unknown Device",
                            address.value ?: "Unknown Address",
                            "passPhrase",
                            System.currentTimeMillis()
                        )
                            .also { device ->
                                _state.value = RemoteDevice.State.Connected(device)
                                log(CONN_TAG, "Device connected: ${device.name} at ${device.address}")
                            }
                    }
                    (it as? Message)?.let {
                        _events.emit(object : RemoteDevice.Event {
                            override val id: Int = it.id
                            override val obj: Any = it
                        })
                    }
                    converters[it::class]?.let { handler -> handler(it) }
                }
        }
    }

    override fun <T> setParameterValue(id: Int, value: T) {
        val newParameter = (_parameters[id] as? DeviceParameter<Any>)
            ?.copy(changeRequestSend = true, value = value as Any)
        _parameters[id] = newParameter as DeviceParameter<*>
        val request = when (value) {
            is Int -> SetIntParameter.newBuilder().setId(id).setValue(value).build()
            is Float -> SetFloatParameter.newBuilder().setId(id).setValue(value).build()
            is String -> SetStringParameter.newBuilder().setId(id)
                .setValue(ByteString.copyFromUtf8(value.trim()))
                .build()

            is Boolean -> SetBooleanParameter.newBuilder().setId(id).setValue(value).build()
            else -> throw IllegalArgumentException("Unsupported parameter type: ${value!!::class.java}")
        }
        logAnalytics("parameter_changed", mapOf("id" to id.toString()))
        log(CONN_TAG, "Scheduled change for parameter $id: $value")

        debounceJobs[id]?.cancel()
        debounceJobs[id] = scope.launch {
            delay(if (value is String) 3000 else 1000)
            log(CONN_TAG, "Value sent for parameter $id: $value")
            connection.sendDataObject(request)
            debounceJobs.remove(id)
        }
    }

    override fun start() {
        connection.start()
    }

    override fun close() {
        connection.close()
        _parameters.clear()
    }

    private val converters: Map<KClass<*>, suspend (Any) -> Unit> = mapOf(
        IntParameter::class to { param ->
            val p = param as IntParameter
            debounceJobs.remove(p.id)
            _parameters[p.id] = (_parameters[p.id] as? DeviceParameter<Int>)
                ?.copy(value = p.value, changeRequestSend = false)
                ?: DeviceParameter(p.id, value = p.value)
        },
        FloatParameter::class to { param ->
            val p = param as FloatParameter
            debounceJobs.remove(p.id)
            _parameters[p.id] = (_parameters[p.id] as? DeviceParameter<Float>)
                ?.copy(value = p.value, changeRequestSend = false)
                ?: DeviceParameter(p.id, value = p.value)
        },
        BooleanParameter::class to { param ->
            val p = param as BooleanParameter
            debounceJobs.remove(p.id)
            _parameters[p.id] = (_parameters[p.id] as? DeviceParameter<Boolean>)
                ?.copy(value = p.value, changeRequestSend = false)
                ?: DeviceParameter(p.id, value = p.value)
        },
        StringParameter::class to { param ->
            val p = param as StringParameter
            val value = p.value.toString(Charset.forName("UTF-8"))
            debounceJobs.remove(p.id)
            _parameters[p.id] = (_parameters[p.id] as? DeviceParameter<String>)
                ?.copy(value = value, changeRequestSend = false)
                ?: DeviceParameter(p.id, value = value)
        }
    )
}
