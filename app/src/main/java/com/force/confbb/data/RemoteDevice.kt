package com.force.confbb.data

import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.force.confbb.model.DeviceParameter
import kotlinx.coroutines.flow.Flow

interface RemoteDevice {
    val state: Flow<State>
    val parameters: SnapshotStateMap<Int, DeviceParameter<*>>
    fun <T> setParameterValue(id: Int, value: T)
    fun close()

    sealed class State {
        data object Connecting : State()
        data object Connected : State()
        data object Disconnected : State()
        data class Error(val error: Throwable) : State()
    }
}
