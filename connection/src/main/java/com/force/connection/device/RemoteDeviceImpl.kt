package com.force.connection.device

import androidx.compose.runtime.mutableStateMapOf
import com.force.confb.pmodel.BooleanParameter
import com.force.confb.pmodel.FloatParameter
import com.force.confb.pmodel.IntParameter
import com.force.confb.pmodel.Message
import com.force.confb.pmodel.ParameterInfo
import com.force.confb.pmodel.StringParameter
import com.force.connection.CONN_TAG
import com.force.connection.ConnectionDefaults.log
import com.force.connection.connection.DeviceConnection
import com.force.model.Device
import com.force.model.DeviceParameter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class RemoteDeviceImpl(
    scope: CoroutineScope,
    private val connection: DeviceConnection
) : RemoteDevice {
    override val name = connection.info.map { info -> info.name }

    override val address = connection.info.map { info -> info.address }

    override val isFast = connection.info.map { info -> info.isFast }

    override val state = connection.state
        .map {
            when (it) {
                is DeviceConnection.State.Connecting -> RemoteDevice.State.Connecting
                is DeviceConnection.State.Disconnected -> RemoteDevice.State.Disconnected
                is DeviceConnection.State.Error -> RemoteDevice.State.Error(it.error)
                is DeviceConnection.State.Connected -> {
                    val device = buildDevice()
                    log(CONN_TAG, "Device connected: ${device.name} at ${device.address}")
                    RemoteDevice.State.Connected(device)
                }
            }
        }

    private val _events = MutableSharedFlow<RemoteDevice.Event>(
        replay = 0,
        extraBufferCapacity = 32,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override val events = _events

    private val _parameters = mutableStateMapOf<Int, DeviceParameter<*>>()

    override val parameters = _parameters

    private val parameterObjectHandler: ParameterObjectHandler = DefaultParameterObjectHandler(
        scope,
        connection::sendDataObject,
        _parameters
    )

    init {
        scope.launch(Dispatchers.IO) {
            observeIncomingData()
        }
    }

    override fun <T> setParameterValue(id: Int, value: T) {
        parameterObjectHandler.setParameterValue(id, value)
    }

    override fun start() {
        connection.start()
    }

    override fun close() {
        parameterObjectHandler.cancel()
        connection.close()
        _parameters.clear()
    }

    private suspend fun observeIncomingData() {
        connection.dataObjects
            .collect {
                when (it) {
                    is ParameterInfo -> parameterObjectHandler.handleInfo(it)
                    is IntParameter,
                    is FloatParameter,
                    is StringParameter,
                    is BooleanParameter -> parameterObjectHandler.handleUpdate(it)

                    is Message -> _events.emit(RemoteDevice.Event.Message(it.text.toStringUtf8()))
                }
            }
    }

    private suspend fun buildDevice(): Device {
        return combine(name, address) { n, a ->
            Device(
                name = n,
                address = a,
                lastSeen = System.currentTimeMillis()
            )
        }.first()
    }
}
