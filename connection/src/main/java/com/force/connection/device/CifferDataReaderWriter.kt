package com.force.connection.device

import android.util.Log
import com.force.confb.pmodel.HandshakeRequest
import com.force.connection.connection.DataReaderWriter
import com.force.connection.stream.ConfDataObjectInputStream
import com.force.connection.stream.ConfDataObjectOutputStream
import com.force.connection.stream.DecodeFrameInputStream
import com.force.connection.stream.EncodeFrameOutputStream
import com.force.connection.stream.TeeFrameErrorInputStream
import com.force.misc.TAG
import java.io.InputStream
import java.io.OutputStream

class CifferDataReaderWriter(
    private val serializer: ConfDataObjectOutputStream.Serializer,
    private val parser: ConfDataObjectInputStream.ObjectParser,
    private val cryptoProducer: CryptoProducer
) : DataReaderWriter {
    private lateinit var confDataObjectInputStream: ConfDataObjectInputStream
    private lateinit var confDataObjectOutputStream: ConfDataObjectOutputStream
    private lateinit var send: (ByteArray) -> Unit
    private lateinit var decrypt: (ByteArray) -> ByteArray
    private lateinit var encrypt: (ByteArray) -> ByteArray

    override fun init(
        input: InputStream,
        outputStream: OutputStream,
        eventStream: OutputStream,
        send: (ByteArray) -> Unit
    ) {
        cryptoProducer.init()
        decrypt = cryptoProducer.getDecrypt()
        encrypt = cryptoProducer.getEncrypt()
        val teeFrameStream = TeeFrameErrorInputStream(input, eventStream)
        val decodeFrameStream = DecodeFrameInputStream(teeFrameStream, decrypt)
        confDataObjectInputStream = ConfDataObjectInputStream(decodeFrameStream, parser, eventStream)
        val encodeFrameStream = EncodeFrameOutputStream(outputStream, encrypt)
        confDataObjectOutputStream = ConfDataObjectOutputStream(encodeFrameStream, serializer)
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

    interface CryptoProducer {
        fun init()
        fun getDecrypt(): (ByteArray) -> ByteArray
        fun getEncrypt(): (ByteArray) -> ByteArray
    }
}
