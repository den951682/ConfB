package com.force.connection.connection

import java.io.InputStream
import java.io.OutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream

class ConnectionSocketIO(
    override val input: InputStream,
    override val output: OutputStream,
) : SocketIO {
    override val eventsInput: PipedInputStream = PipedInputStream()
    private val pipeOut = PipedOutputStream(eventsInput)
    override val eventsOutput: OutputStream get() = pipeOut

    override fun close() {
        input.close()
        output.close()
        pipeOut.close()
    }
}
