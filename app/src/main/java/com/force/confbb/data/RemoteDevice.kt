package com.force.confbb.data

import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.force.confbb.model.DeviceParameter
import kotlinx.coroutines.flow.Flow

interface RemoteDevice {
    val state: Flow<State>
    val parameters: SnapshotStateMap<Int, DeviceParameter<*>>
    fun close()

    enum class State {
        CONNECTING,
        CONNECTED,
        DISCONNECTED,
        ERROR
    }
}
