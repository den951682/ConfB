package com.force.confbb.data

import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
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
import com.force.confbb.model.DataType
import com.force.confbb.model.Device
import com.force.confbb.model.DeviceConnectionStatus
import com.force.confbb.model.DeviceParameter
import com.force.confbb.util.TAG
import com.google.protobuf.ByteString
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.nio.charset.Charset
import kotlin.reflect.KClass


class BluetoothRemoteDevice @AssistedInject constructor(
    @Assisted("address") address: String,
    @Assisted("pass") val passPhrase: String,
    @Assisted val scope: CoroutineScope,
    connectionFactory: CipherBluetoothDeviceConnection.Factory,
    savedDevicesRepository: SavedDevicesRepository
) : RemoteDevice {
    private val connection = connectionFactory.create(
        deviceAddress = address,
        passPhrase = passPhrase,
        scope = scope
    )

    private val _parameters = mutableStateMapOf<Int, DeviceParameter<*>>()

    private val _events = MutableSharedFlow<RemoteDevice.Event>(
        replay = 0,
        extraBufferCapacity = 32,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override val name: String
        get() = connection.credentials.first

    override val state: StateFlow<RemoteDevice.State> = connection.data.map {
        when (it) {
            is DeviceConnectionStatus.Disconnected -> RemoteDevice.State.Disconnected
            is DeviceConnectionStatus.Connected -> RemoteDevice.State.Connected
            is DeviceConnectionStatus.Error -> RemoteDevice.State.Error(it.error)
            else -> RemoteDevice.State.Connected
        }
    }.stateIn(
        scope = scope,
        started = WhileSubscribed(5000),
        initialValue = RemoteDevice.State.Disconnected
    )

    override val parameters: SnapshotStateMap<Int, DeviceParameter<*>> = _parameters

    override val events: SharedFlow<RemoteDevice.Event> = _events

    private val debounceJobs = mutableMapOf<Int, Job>()

    init {
        scope.launch(Dispatchers.IO) {
            connection.data
                .filterIsInstance<DeviceConnectionStatus.DataMessage>()
                .mapNotNull { it.data }
                .collect { parameter ->
                    val handler = converters[parameter::class]
                    if (handler != null) {
                        handler(parameter)
                    } else {
                        Log.d(TAG, "Unsupported parameter type: ${parameter::class}")
                    }
                }
        }
        scope.launch(Dispatchers.IO) {
            connection.data
                .filter { it is DeviceConnectionStatus.DataMessage }
                .map { it as DeviceConnectionStatus.DataMessage }
                .map { it.data }
                .filter { it is ParameterInfo }
                .map { it as ParameterInfo }
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
            connection.data.collect {
                if (it is DeviceConnectionStatus.DataMessage) {
                    (it.data as? HandshakeResponse)?.let {
                        savedDevicesRepository.addDevice(
                            Device(
                                connection.credentials.first,
                                connection.credentials.second,
                                passPhrase,
                                System.currentTimeMillis()
                            )
                        )
                        savedDevicesRepository.setLastSeen(connection.credentials.second, System.currentTimeMillis())
                        savedDevicesRepository.setName(connection.credentials.second, connection.credentials.first)
                    }
                    (it.data as? Message)?.let {
                        _events.emit(object : RemoteDevice.Event {
                            override val id: Int = it.id
                            override val obj: Any = it
                        })
                    }
                }
            }
        }
    }

    override fun <T> setParameterValue(id: Int, value: T) {
        val newParameter = (_parameters[id] as? DeviceParameter<Any>)
            ?.copy(changeRequestSend = true, value = value as Any)
        _parameters[id] = newParameter as DeviceParameter<*>
        val (type, request) = when (value) {
            is Int -> DataType.SetInt to SetIntParameter.newBuilder().setId(id).setValue(value).build()
            is Float -> DataType.SetFloat to SetFloatParameter.newBuilder().setId(id).setValue(value).build()
            is String -> DataType.SetString to SetStringParameter.newBuilder().setId(id)
                .setValue(ByteString.copyFromUtf8(value.trim()))
                .build()

            is Boolean -> DataType.SetBoolean to SetBooleanParameter.newBuilder().setId(id).setValue(value).build()
            else -> throw IllegalArgumentException("Unsupported parameter type: ${value!!::class.java}")
        }
        Log.d(TAG, "Scheduled change for parameter $id: $value")

        debounceJobs[id]?.cancel()
        debounceJobs[id] = scope.launch {
            delay(if (value is String) 3000 else 1000)
            Log.d(TAG, "Value sent for parameter $id: $value")
            connection.send(byteArrayOf(type.code) + request.toByteArray())
            debounceJobs.remove(id)
        }
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


    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("address") deviceAddress: String,
            @Assisted("pass") passPhrase: String,
            scope: CoroutineScope
        ): BluetoothRemoteDevice
    }
}
