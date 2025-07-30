package com.force.confbb.data.streams

import com.force.confbb.model.ConfError

class DecodeFrameInputStream(
    private val frameInputStream: TeeFrameErrorInputStream,
    private val decrypt: (ByteArray) -> ByteArray
) {
    fun readDecryptedFrame(): ByteArray {
        return frameInputStream.readEncryptedFrame().let { encFrame ->
            try {
                decrypt(encFrame)
            } catch (ex: Exception) {
                throw ConfError.DecryptError()
            }
        }
    }
}
