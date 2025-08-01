package com.force.connection.connection

import java.io.InputStream
import java.io.OutputStream

interface SocketIO {
    val input: InputStream
    val output: OutputStream
    val eventsOutput: OutputStream
    val eventsInput: InputStream
    fun close()
}
