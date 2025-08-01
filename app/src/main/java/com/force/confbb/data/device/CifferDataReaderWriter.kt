package com.force.confbb.data.device

import ConfDataObjectOutputStream
import android.util.Log
import com.force.confb.pmodel.HandshakeRequest
import com.force.connection.connection.AbstractDeviceConnection
import com.force.connection.stream.ConfDataObjectInputStream
import com.force.connection.stream.DecodeFrameInputStream
import com.force.connection.stream.EncodeFrameOutputStream
import com.force.connection.stream.TeeFrameErrorInputStream
import com.force.misc.TAG
import java.io.InputStream
import java.io.OutputStream

class CifferDataReaderWriter(
    private val decrypt: (ByteArray) -> ByteArray,
    private val encrypt: (ByteArray) -> ByteArray
) : AbstractDeviceConnection.DataReaderWriter {
    private lateinit var confDataObjectInputStream: ConfDataObjectInputStream
    private lateinit var confDataObjectOutputStream: ConfDataObjectOutputStream
    private lateinit var send: (ByteArray) -> Unit

    override fun init(
        input: InputStream,
        outputStream: OutputStream,
        eventStream: OutputStream,
        send: (ByteArray) -> Unit
    ) {
        val teeFrameStream = TeeFrameErrorInputStream(input, eventStream)
        val decodeFrameStream = DecodeFrameInputStream(teeFrameStream, decrypt)
        confDataObjectInputStream = ConfDataObjectInputStream(decodeFrameStream, eventStream)
        val encodeFrameStream = EncodeFrameOutputStream(outputStream, encrypt)
        confDataObjectOutputStream = ConfDataObjectOutputStream(encodeFrameStream)
        this.send = send
        val guardText = "guard\n"
        val handShake = HandshakeRequest.newBuilder().setText("HANDSHAKE").build()
        send(guardText.toByteArray())
        Log.d(TAG, "Sending handshake")
        sendDataObject(handShake)
    }

    override fun readDataObject(): Any = confDataObjectInputStream.readDataObject()

    override fun sendDataObject(dataObject: Any) {
        confDataObjectOutputStream.writeDataObject(dataObject)
    }
}
