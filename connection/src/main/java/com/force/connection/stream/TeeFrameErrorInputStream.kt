package com.force.connection.stream

import com.force.connection.CONN_TAG
import com.force.connection.ConnectionDefaults.log
import com.force.model.ConfException
import java.io.DataInputStream
import java.io.EOFException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class TeeFrameErrorInputStream(
    private val input: InputStream,
    private val errorOutput: OutputStream
) : DataInputStream(input) {

    @Throws(ConfException::class, IOException::class)
    fun readEncryptedFrame(): ByteArray {
        while (true) {
            input.read().let { b ->
                if (b < 28) {
                    val error = ConfException.fromCode(b)
                    if (error.isCritical) {
                        throw error
                    } else {
                        log(CONN_TAG, "Received error code: $b, message: ${error.message}")
                        errorOutput.write(b)
                    }
                } else {
                    val frame = ByteArray(b)
                    readFrame(frame)
                    return frame
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun readFrame(buffer: ByteArray) {
        var read = 0
        while (read < buffer.size) {
            val r = input.read(buffer, read, buffer.size - read)
            if (r < 0) throw EOFException("Stream closed while reading ${buffer.size} bytes")
            read += r
        }
    }
}
