package com.force.connection.stream

import java.io.FilterOutputStream
import java.io.OutputStream

class ConfDataObjectOutputStream(
    private val outputStream: OutputStream,
    private val serializer: Serializer
) : FilterOutputStream(outputStream) {
    fun writeDataObject(dataObject: Any) {
        val data = serializer.serialize(dataObject)
        if (outputStream is EncodeFrameOutputStream) {
            outputStream.writeData(data)
        } else {
            write(data)
        }
    }

    interface Serializer {
        fun serialize(dataObject: Any): ByteArray
    }
}
