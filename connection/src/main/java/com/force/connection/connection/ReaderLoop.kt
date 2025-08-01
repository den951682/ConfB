package com.force.connection.connection

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive

class ReaderLoop(
    private val scope: CoroutineScope,
    private val reader: DataReaderWriter,
    private val onRead: suspend (Any) -> Unit,
    private val onError: suspend (Throwable) -> Unit
) {
    suspend fun start() {
        try {
            while (currentCoroutineContext().isActive) {
                val obj = reader.readDataObject()
                onRead(obj)
            }
        } catch (e: Exception) {
            onError(e)
        }
    }
}
