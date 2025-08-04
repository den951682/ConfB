package com.force.connection.connection

import com.force.connection.CONN_TAG
import com.force.connection.ConnectionDefaults.log
import com.force.model.ConfException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

class ConnectionLifecycleManager {
    private val _state = MutableStateFlow<DeviceConnection.State>(DeviceConnection.State.Connecting)
    val state: StateFlow<DeviceConnection.State> get() = _state

    fun transitionTo(newState: DeviceConnection.State) {
        log(CONN_TAG, "→ $newState")
        _state.value = newState
    }

    fun handleError(ex: Throwable) {
        if (state.value !is DeviceConnection.State.Error && state.value !is DeviceConnection.State.Disconnected) {
            log(CONN_TAG, "Lifecycle Connection error: ${ex.message}")
            val state = when (ex) {
                is CancellationException -> DeviceConnection.State.Disconnected
                is IOException -> DeviceConnection.State.Error(ConfException.SocketException())
                is ConfException -> DeviceConnection.State.Error(ex)
                else -> DeviceConnection.State.Error(ConfException.UnknownException(ex.message ?: ""))
            }
            transitionTo(state)
        }
    }
}
