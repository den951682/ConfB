package com.force.confbb.data

import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.force.confb.pmodel.IntParameter
import com.force.confb.pmodel.ParameterInfo
import com.force.confb.pmodel.SetBooleanParameter
import com.force.confb.pmodel.SetFloatParameter
import com.force.confb.pmodel.SetIntParameter
import com.force.confb.pmodel.SetStringParameter
import com.force.confbb.model.DataType
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class BluetoothRemoteDevice @AssistedInject constructor(
    @Assisted address: String,
    @Assisted val scope: CoroutineScope,
    connectionFactory: CipherBluetoothDeviceConnection.Factory
) : RemoteDevice {
    private val connection = connectionFactory.create(
        deviceAddress = address,
        scope = scope
    )

    private val _parameters = mutableStateMapOf<Int, DeviceParameter<*>>()

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

    private val debounceJobs = mutableMapOf<Int, Job>()

    init {
        scope.launch(Dispatchers.IO) {
            connection.data
                .filterIsInstance<DeviceConnectionStatus.DataMessage>()
                .mapNotNull { it.data as? IntParameter }
                .collect { parameter ->
                    debounceJobs.remove(parameter.id)
                    _parameters[parameter.id] = (_parameters[parameter.id] as? DeviceParameter<Int>)
                        ?.copy(value = parameter.value, changeRequestSend = false)
                        ?: DeviceParameter(parameter.id, value = parameter.value)
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
    }

    override fun <T> setParameterValue(id: Int, value: T) {
        val newParameter = (_parameters[id] as? DeviceParameter<Any>)
            ?.copy(changeRequestSend = true, value = value as Any)
        _parameters[id] = newParameter as DeviceParameter<*>
        val (type, request) = when (value) {
            is Int -> DataType.SetInt to SetIntParameter.newBuilder().setId(id).setValue(value).build()
            is Float -> DataType.SetFloat to SetFloatParameter.newBuilder().setId(id).setValue(value).build()
            is String -> DataType.SetString to SetStringParameter.newBuilder().setId(id)
                .setValue(ByteString.copyFromUtf8(value))
                .build()

            is Boolean -> DataType.SetBoolean to SetBooleanParameter.newBuilder().setId(id).setValue(value).build()
            else -> throw IllegalArgumentException("Unsupported parameter type: ${value!!::class.java}")
        }
        Log.d(TAG, "Scheduled change for parameter $id: $value")

        debounceJobs[id]?.cancel()
        debounceJobs[id] = scope.launch {
            delay(1500)
            Log.d(TAG, "Value sent for parameter $id: $value")
            connection.send(byteArrayOf(type.code) + request.toByteArray())
            debounceJobs.remove(id)
        }
    }

    override fun close() {
        connection.close()
        _parameters.clear()
    }

    @AssistedFactory
    interface Factory {
        fun create(
            deviceAddress: String,
            scope: CoroutineScope
        ): BluetoothRemoteDevice
    }
}
