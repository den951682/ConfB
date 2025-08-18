package com.force.connection.protocol

import android.util.Base64
import com.force.confb.pmodel.HandshakeRequest
import com.force.connection.CONN_TAG
import com.force.connection.ConnectionDefaults.log
import com.force.model.ConfException
import com.force.model.DataType
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import java.io.EOFException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class EcdhAesProtocol(
    private val serializer: ProtocolSerializer,
    private val parser: ProtocolParser,
    private val cryptoProducer: CryptoProducer,
    private val header: ByteArray? = null,
) : Protocol {
    override val events = MutableSharedFlow<ProtocolEvent>(
        replay = 8,
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private lateinit var input: InputStream
    private lateinit var output: OutputStream
    private lateinit var encrypt: (ByteArray) -> ByteArray
    private lateinit var decrypt: (ByteArray) -> ByteArray

    private var headerIsRead = false
    private var handshakeIsReceived = false
    private var errorCount = 0

    override suspend fun init(input: InputStream, output: OutputStream) {
        log(CONN_TAG, "Initializing EcdhAesProtocol {$input} {$output}")
        headerIsRead = false
        handshakeIsReceived = false
        this.input = input
        this.output = output
        cryptoProducer.init()
        if (header != null) {
            output.write(header.size)
            output.write(header)
            log(CONN_TAG, "Sending header: ${header.decodeToString()}")
        } else {
            log(CONN_TAG, "No header")
            output.write(0)
        }
        log(CONN_TAG, "Sending handshake with public key")
        val keyText = Base64.encodeToString(cryptoProducer.getPublic(), Base64.NO_WRAP)
        val handShake = HandshakeRequest.newBuilder().setText(keyText).build().toByteArray()
        val toSend = byteArrayOf(handShake.size.toByte()) + handShake
        log(CONN_TAG, "handshake to send: " + toSend.joinToString { String.format(":%x", it)})
        output.write(toSend)
        output.flush()
    }

    override suspend fun receive(): Any? {
        if (!headerIsRead) {
            readHeader()
            headerIsRead = true
        }
        val encryptedFrame = readEncryptedFrame()
        val decryptedFrame = if (handshakeIsReceived) {
            decryptFrame(encryptedFrame)
        } else {
            encryptedFrame
        }
        val obj = parseFrame(decryptedFrame)
        if (!handshakeIsReceived) {
            if (obj is HandshakeRequest) {
                handshakeIsReceived = true
                log(CONN_TAG, "Handshake received with key")
                val key = Base64.decode(obj.text, Base64.NO_WRAP)
                cryptoProducer.applyOtherPublic(key)
                decrypt = cryptoProducer.getDecrypt()
                encrypt = cryptoProducer.getEncrypt()
                events.emit(ProtocolEvent.Ready)
                return receive()
            } else {
                log(CONN_TAG, "Unexpected object received: $obj")
                throw ConfException.ProtoDecodeException()
            }
        }
        return obj
    }

    override suspend fun send(data: Any) {
        awaitReady()
        val bytes = serializer.serialize(data)
        val encrypted = encrypt(bytes)
        if (encrypted.size > 255) {
            throw ConfException.DataToLarge("Encrypt")
        }
        val toSend = byteArrayOf(encrypted.size.toByte()) + encrypted
        output.write(toSend)
        output.flush()
    }

    private suspend fun readHeader() {
        val size = input.read()
        if (size < 0) throw EOFException("Stream closed while reading header")
        if (size == 0) {
            log(CONN_TAG, "No header received")
        } else {
            val headerBytes = ByteArray(size)
            readFully(headerBytes)
            log(CONN_TAG, "Received header: ${headerBytes.decodeToString()}")
            events.emit(ProtocolEvent.Header(headerBytes))
        }
    }

    private suspend fun parseFrame(frame: ByteArray): Any? {
        return try {
            if (!handshakeIsReceived) {
                try {
                   return HandshakeRequest.parseFrom(frame)
                } catch (ex: Exception) {
                    log(CONN_TAG, "Failed to parse handshake: ${ex.message}")
                }
            }
            parser.parse(frame)
        } catch (ex: Exception) {
            events.emit(ProtocolEvent.Error(ex))
            return null
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
                    errorCount++
                    val error = ConfException.fromCode(b)
                    if (error.isCritical) {
                        throw error
                    } else {
                        log(CONN_TAG, "Received error code: $b, message: ${error.message}")
                        events.emit(ProtocolEvent.Error(error))
                    }
                    if (errorCount > 5) {
                        log(CONN_TAG, "Too many errors received, resetting connection")
                        errorCount = 0
                        throw ConfException.ToManyErrorsExceptions()
                    }
                } else {
                    errorCount = 0
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

    interface CryptoProducer {
        fun init()
        fun getPublic(): ByteArray
        fun applyOtherPublic(publicKey: ByteArray)
        fun getDecrypt(): (ByteArray) -> ByteArray
        fun getEncrypt(): (ByteArray) -> ByteArray
    }
}
