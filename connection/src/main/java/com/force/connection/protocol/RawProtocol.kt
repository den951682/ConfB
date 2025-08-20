package com.force.connection.protocol

import com.force.confb.pmodel.HandshakeRequest
import com.force.connection.CONN_TAG
import com.force.connection.ConnectionDefaults.log
import com.force.model.ConfException
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import java.io.EOFException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class RawProtocol(
    private val serializer: ProtocolSerializer,
    private val parser: ProtocolParser,
    private val bindPhraseProducer: BindPhraseProducer,
    //не треба, щоб дві сторони перевіряли фразу, достатньо перевірити з одної сторони
    private val checkBindPhrase: Boolean,
    private val header: ByteArray? = null,
) : Protocol {
    override val events = MutableSharedFlow<ProtocolEvent>(
        replay = 8,
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private lateinit var input: InputStream
    private lateinit var output: OutputStream
    private lateinit var phrase: String

    private var headerIsRead = false
    private var handshakeIsReceived = false

    override suspend fun init(input: InputStream, output: OutputStream) {
        log(CONN_TAG, "Initializing RawProtocol {$input} {$output}")
        headerIsRead = false
        handshakeIsReceived = false
        this.input = input
        this.output = output
        if (header != null) {
            output.write(header.size)
            output.write(header)
            log(CONN_TAG, "Sending header: ${header.decodeToString()}")
        } else {
            log(CONN_TAG, "No header")
            output.write(0)
        }
        if(!checkBindPhrase) sendHandshake()
    }

    private fun sendHandshake() {
        log(CONN_TAG, "Sending handshake")
        val handshakeText = if(checkBindPhrase) "HANDSHAKE" else bindPhraseProducer.getBindPhrase()
        val handShake = HandshakeRequest.newBuilder().setText(handshakeText).build().toByteArray()
        val toSend = byteArrayOf(handShake.size.toByte()) + handShake
        output.write(toSend)
        output.flush()
    }

    override suspend fun receive(): Any? {
        if (!headerIsRead) {
            readHeader()
            headerIsRead = true
        }
        val frame = readFrame()
        val obj = parseFrame(frame)
        if (!handshakeIsReceived) {
            if (obj is HandshakeRequest) {
                handshakeIsReceived = true
                log(CONN_TAG, "Handshake received: ${obj.text}")
                if(checkBindPhrase) {
                    if(obj.text != bindPhraseProducer.getBindPhrase()) {
                        //допущення, що не будуть пересилатись фрейми розміром 1 байт, наступним байтом має бути розмір фрейму, ставим 1 як індикатор помилки
                        output.write(byteArrayOf(0x1))
                        output.flush()
                        delay(10)
                        throw ConfException.BindPhraseException()
                    } else {
                        sendHandshake()
                    }
                }
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
        val toSend = byteArrayOf(bytes.size.toByte()) + bytes
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

    private suspend fun readFrame(): ByteArray {
        while (true) {
            input.read().let { b ->
                //todo деколи не відображається повідомлення про цю помилку, хоча в логах вона є
                if(b == 1) throw ConfException.BindPhraseException()
                val frame = ByteArray(b)
                readFully(frame)
                return frame
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

    interface BindPhraseProducer {
        fun getBindPhrase(): String
    }
}
