package com.force.connection.stream

import com.force.connection.CONN_TAG
import com.force.connection.ConnectionDefaults.log
import com.force.model.ConfException
import com.force.model.ConfException.Companion.toCode
import java.io.OutputStream

class ConfDataObjectInputStream(
    private val decodeFrameInputStream: DecodeFrameInputStream,
    private val parser: ObjectParser,
    private val errorOutput: OutputStream
) {
    fun readDataObject(): Any {
        decodeFrameInputStream.readDecryptedFrame().let { frame ->
            val dataType = frame[0]
            val dataToParse = frame.drop(1).toByteArray()
            return try {
                parser.parse(dataType, dataToParse)
            } catch (ex: Exception) {
                log(CONN_TAG, "Unhandled received data type: ${frame[0]}")
                errorOutput.write(ConfException.NotSupportedException().toCode())
            }
        }
    }

    interface ObjectParser {
        fun parse(type: Byte, data: ByteArray): Any
    }
}
