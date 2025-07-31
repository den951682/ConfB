package com.force.confbb.data.streams

import java.io.FilterOutputStream
import java.io.OutputStream

class EncodeFrameOutputStream(
    private val outputStream: OutputStream,
    private val encrypt: (ByteArray) -> ByteArray
) : FilterOutputStream(outputStream) {

    fun writeData(b: ByteArray) {
        val encrypted = encrypt(b)
        write(byteArrayOf(encrypted.size.toByte()) + encrypted)
    }
}
