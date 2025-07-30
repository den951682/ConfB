package com.force.confbb.data.device

import java.io.InputStream
import java.io.OutputStream

class CifferDataReaderWriter : AbstractDeviceConnection.DataReaderWriter {
    override fun init(
        input: InputStream,
        outputStream: OutputStream,
        eventStream: OutputStream,
        send: (ByteArray) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun readDataObject(): Any {
        TODO("Not yet implemented")
    }

    override fun sendDataObject(dataObject: Any) {
        TODO("Not yet implemented")
    }

}
