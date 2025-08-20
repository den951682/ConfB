package com.force.connection.connection

import com.force.connection.ConnectionEvent
import kotlinx.coroutines.flow.Flow

interface DeviceConnection {
    val info: Flow<Info>
    val dataObjects: Flow<Any>
    val state: Flow<State>
    val events: Flow<ConnectionEvent>
    fun start()
    suspend fun sendDataObject(dataObject: Any)
    fun close()

    sealed class State {
        data object Connecting : State()
        data object Connected : State()
        data object Disconnected : State()
        data class Error(val error: Exception) : State()
    }

    data class Info(
        val type: Type,
        val address: String,
        val name: String,
        val isFast: Boolean
    )

    enum class Type {
        Bluetooth,
        WifiClient,
        WifiServer,
        Serial,
        Network
    }
}