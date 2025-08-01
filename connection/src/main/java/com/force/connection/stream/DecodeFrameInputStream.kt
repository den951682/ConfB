package com.force.connection.stream

import com.force.model.ConfException

class DecodeFrameInputStream(
    private val frameInputStream: TeeFrameErrorInputStream,
    private val decrypt: (ByteArray) -> ByteArray
) {
    fun readDecryptedFrame(): ByteArray {
        return frameInputStream.readEncryptedFrame().let { encFrame ->
            try {
                decrypt(encFrame)
            } catch (ex: Exception) {
                throw ConfException.DecryptException()
            }
        }
    }
}
