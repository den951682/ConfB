package com.force.confbb.data

import com.force.confbb.model.DeviceConnectionStatus
import kotlinx.coroutines.flow.Flow
import java.io.InputStream

interface DeviceConnection {
    val data: Flow<DeviceConnectionStatus>
    suspend fun listenInputStream(input: InputStream, isActive: () -> Boolean)
    fun send(data: ByteArray)
    fun close(exception: Throwable? = null)
    fun close() {}
}
