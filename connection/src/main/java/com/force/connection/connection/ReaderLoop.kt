package com.force.connection.connection

import com.force.connection.protocol.Protocol
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive

class ReaderLoop(
    private val protocol: Protocol,
    private val onRead: suspend (Any) -> Unit,
    private val onError: suspend (Throwable) -> Unit
) {
    suspend fun start() {
        try {
            while (currentCoroutineContext().isActive) {
                val obj = protocol.receive()
                onRead(obj)
            }
        } catch (e: Exception) {
            onError(e)
        }
    }
}
