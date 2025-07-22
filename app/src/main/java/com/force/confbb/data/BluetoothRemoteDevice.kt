package com.force.confbb.data

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.force.confb.pmodel.ParameterInfo
import com.force.confbb.model.DeviceConnectionStatus
import com.force.confbb.model.DeviceParameter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class BluetoothRemoteDevice @AssistedInject constructor(
    @Assisted address: String,
    @Assisted scope: CoroutineScope,
    connectionFactory: CipherBluetoothDeviceConnection.Factory
) : RemoteDevice {
    private val connection = connectionFactory.create(
        deviceAddress = address,
        scope = scope
    )

    private val _parameters = mutableStateMapOf<Int, DeviceParameter<*>>()

    override val state: StateFlow<RemoteDevice.State> = connection.data.map {
        when (it) {
            is DeviceConnectionStatus.Disconnected -> RemoteDevice.State.DISCONNECTED
            is DeviceConnectionStatus.Connected -> RemoteDevice.State.CONNECTING
            is DeviceConnectionStatus.Error -> RemoteDevice.State.ERROR
            else -> RemoteDevice.State.CONNECTED
        }
    }.stateIn(
        scope = scope,
        started = WhileSubscribed(5000),
        initialValue = RemoteDevice.State.DISCONNECTED
    )

    override val parameters: SnapshotStateMap<Int, DeviceParameter<*>> = _parameters

    init {
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
                                ep?.minValue ?: parameterInfo.minValue,
                                ep?.maxValue ?: parameterInfo.maxValue,
                                ep?.editable ?: parameterInfo.editable
                            )

                            1 -> DeviceParameter(
                                parameterInfo.id,
                                ep?.value ?: 0f,
                                ep?.name ?: parameterInfo.name.toStringUtf8(),
                                ep?.description ?: parameterInfo.description.toStringUtf8(),
                                ep?.minValue ?: parameterInfo.minValue,
                                ep?.maxValue ?: parameterInfo.maxValue,
                                ep?.editable ?: parameterInfo.editable
                            )

                            2 -> DeviceParameter(
                                parameterInfo.id,
                                ep?.value ?: "",
                                ep?.name ?: parameterInfo.name.toStringUtf8(),
                                ep?.description ?: parameterInfo.description.toStringUtf8(),
                                editable = ep?.editable ?: parameterInfo.editable
                            )

                            else -> DeviceParameter(
                                parameterInfo.id,
                                ep?.value ?: false,
                                ep?.name ?: parameterInfo.name.toStringUtf8(),
                                ep?.description ?: parameterInfo.description.toStringUtf8(),
                                editable = ep?.editable ?: parameterInfo.editable
                            )

                        }
                    }
                }
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
