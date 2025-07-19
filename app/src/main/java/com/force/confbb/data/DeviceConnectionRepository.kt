package com.force.confbb.data

import com.force.confbb.model.DeviceConnectionStatus
import kotlinx.coroutines.flow.SharedFlow

interface DeviceConnectionRepository {
    val data: SharedFlow<DeviceConnectionStatus>
    fun send(data: ByteArray)
    fun close(exception: Throwable? = null)
}
