package com.force.connection.protocol

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import java.io.InputStream
import java.io.OutputStream

interface Protocol {
    val events: Flow<ProtocolEvent>
    suspend fun awaitReady() {
        events.filter { it is ProtocolEvent.Ready }.first()
    }
    suspend fun init(input: InputStream, output: OutputStream)
    suspend fun receive(): Any?
    suspend fun send(data: Any)
}
