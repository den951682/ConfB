package com.force.connection.protocol

import kotlinx.coroutines.flow.Flow
import java.io.InputStream
import java.io.OutputStream

interface Protocol {
    val events: Flow<Any>
    suspend fun init(input: InputStream, output: OutputStream)
    suspend fun receive(): Any
    suspend fun send(data: Any)
}
