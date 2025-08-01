package com.force.connection.device

import com.force.model.Device
import com.force.model.DeviceParameter
import kotlinx.coroutines.flow.Flow

interface RemoteDevice {
    val name: Flow<String?>
    val address: Flow<String?>
    val state: Flow<State>
    val events: Flow<Event>
    val parameters: Map<Int, DeviceParameter<*>>
    fun <T> setParameterValue(id: Int, value: T)
    fun start()
    fun close()

    sealed class State {
        data object Connecting : State()
        data class Connected(val device: Device) : State()
        data object Disconnected : State()
        data class Error(val error: Throwable) : State()
    }

    interface Event {
        val id: Int
        val obj: Any
    }
}
