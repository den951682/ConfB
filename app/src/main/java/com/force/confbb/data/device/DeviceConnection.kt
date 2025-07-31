package com.force.confbb.data.device

import com.force.confbb.model.ConfError
import kotlinx.coroutines.flow.Flow

interface DeviceConnection {
    val info: Info
    val dataObjects: Flow<Any>
    val state: Flow<State>
    val events: Flow<Event>
    fun start()
    fun sendDataObject(dataObject: Any)
    fun close()

    sealed class State {
        data object Connecting : State()
        data object Connected : State()
        data object Disconnected : State()
        data class Error(val error: Exception) : State()
    }

    sealed class Event {
        data class Error(val confError: ConfError) : Event()
    }

    data class Info(
        val type: Type,
        val address: String,
        val name: String
    )

    enum class Type {
        Bluetooth,
        WiFi,
        Serial,
        Network
    }
}
