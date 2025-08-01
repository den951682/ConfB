package com.force.confbb.data.device

import com.force.connection.connection.AbstractDeviceConnection
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream

class PlainDataReaderWriter : AbstractDeviceConnection.DataReaderWriter {
    private lateinit var bufferedReader: BufferedReader
    private lateinit var send: (ByteArray) -> Unit
    override fun init(
        input: InputStream,
        outputStream: OutputStream,
        eventStream: OutputStream,
        send: (ByteArray) -> Unit
    ) {
        bufferedReader = BufferedReader(InputStreamReader(input))
        this.send = send
    }

    override fun readDataObject(): String = bufferedReader.readLine()

    override fun sendDataObject(dataObject: Any) {
        send(dataObject.toString().toByteArray())
    }
}
