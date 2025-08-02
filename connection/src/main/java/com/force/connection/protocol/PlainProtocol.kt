package com.force.connection.protocol

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream

class PlainProtocol() : Protocol {
    override val events: Flow<Any> = flowOf()
    private lateinit var input: InputStream
    private lateinit var output: OutputStream
    private lateinit var reader: BufferedReader

    override suspend fun init(input: InputStream, output: OutputStream) {
        this.input = input
        this.output = output
        reader = BufferedReader(InputStreamReader(input))
    }

    override suspend fun read(): String {
        return reader.readLine()
    }

    override suspend fun send(data: Any) {
        val bytes = data.toString().toByteArray()
        output.write(bytes)
        output.flush()
    }
}
