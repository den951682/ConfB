package com.force.connection.protocol

import android.util.Log
import com.force.confb.pmodel.HandshakeRequest
import com.force.connection.CONN_TAG
import com.force.connection.ConnectionDefaults.log
import com.force.misc.TAG
import com.force.model.ConfException
import kotlinx.coroutines.flow.MutableSharedFlow
import java.io.EOFException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class PassPhraseAesProtocol(
    private val serializer: Serializer,
    private val parser: Parser,
    private val cryptoProducer: CryptoProducer
) : Protocol {
    override val events = MutableSharedFlow<Any>()
    private lateinit var input: InputStream
    private lateinit var output: OutputStream
    private lateinit var encrypt: (ByteArray) -> ByteArray
    private lateinit var decrypt: (ByteArray) -> ByteArray

    override suspend fun init(input: InputStream, output: OutputStream) {
        this.input = input
        this.output = output
        cryptoProducer.init()
        encrypt = cryptoProducer.getEncrypt()
        decrypt = cryptoProducer.getDecrypt()
        val guardText = "guard\n"
        val handShake = HandshakeRequest.newBuilder().setText("HANDSHAKE").build()
        output.write(guardText.toByteArray())
        Log.d(TAG, "Sending handshake")
        send(handShake)
    }

    override suspend fun receive(): Any {
        val encryptedFrame = readEncryptedFrame()
        val decryptedFrame = decryptFrame(encryptedFrame)
        val dataType = decryptedFrame[0]
        val dataToParse = decryptedFrame.drop(1).toByteArray()
        return parseFrame(dataType, dataToParse)
    }

    override suspend fun send(data: Any) {
        val bytes = serializer.serialize(data)
        val encrypted = encrypt(bytes)
        val toSend = byteArrayOf(encrypted.size.toByte()) + encrypted
        output.write(toSend)
        output.flush()
    }

    private suspend fun parseFrame(dataType: Byte, frame: ByteArray): Any {
        return try {
            parser.parse(dataType, frame)
        } catch (ex: Exception) {
            log(CONN_TAG, "Unhandled received data type: $dataType")
            events.emit(ConfException.NotSupportedException())
        }
    }

    private fun decryptFrame(frame: ByteArray): ByteArray {
        return try {
            decrypt(frame)
        } catch (ex: Exception) {
            throw ConfException.DecryptException()
        }
    }

    private suspend fun readEncryptedFrame(): ByteArray {
        while (true) {
            input.read().let { b ->
                if (b < 28) {
                    val error = ConfException.fromCode(b)
                    if (error.isCritical) {
                        throw error
                    } else {
                        log(CONN_TAG, "Received error code: $b, message: ${error.message}")
                        events.emit(error)
                    }
                } else {
                    val frame = ByteArray(b)
                    readFully(frame)
                    return frame
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun readFully(buffer: ByteArray) {
        var read = 0
        while (read < buffer.size) {
            val r = input.read(buffer, read, buffer.size - read)
            if (r < 0) throw EOFException("Stream closed while reading ${buffer.size} bytes")
            read += r
        }
    }

    interface Serializer {
        fun serialize(data: Any): ByteArray
    }

    interface Parser {
        fun parse(type: Byte, data: ByteArray): Any
    }

    interface CryptoProducer {
        fun init()
        fun getDecrypt(): (ByteArray) -> ByteArray
        fun getEncrypt(): (ByteArray) -> ByteArray
    }
}
