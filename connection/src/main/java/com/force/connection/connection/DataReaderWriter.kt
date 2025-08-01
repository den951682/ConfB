package com.force.connection.connection

import java.io.InputStream
import java.io.OutputStream

interface DataReaderWriter {
    fun init(input: InputStream, outputStream: OutputStream, eventStream: OutputStream, send: (ByteArray) -> Unit)
    fun readDataObject(): Any
    fun sendDataObject(dataObject: Any)
}
