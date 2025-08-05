package com.force.crypto

import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.spec.X509EncodedKeySpec
import javax.crypto.KeyAgreement

class CryptoEcdh {
    private lateinit var keyPair: KeyPair
    private lateinit var keyAgreement: KeyAgreement
    private lateinit var sharedSecret: ByteArray
    private lateinit var cryptoAes: CryptoAes

    init {
        generateKeyPair()
    }

    fun getPublicKey(): ByteArray {
        return keyPair.public.encoded
    }

    fun applyOtherPublic(otherPublicKey: ByteArray) {
        computeSharedSecret(otherPublicKey)
        cryptoAes = CryptoAes(key = generateAesKey(sharedSecret))
    }

    fun decryptData(encrypted: ByteArray): ByteArray {
        return cryptoAes.decryptData(encrypted)
    }

    fun encryptData(data: ByteArray): ByteArray {
        return cryptoAes.encryptDataWhole(data)
    }

    private fun generateKeyPair() {
        val kpg = KeyPairGenerator.getInstance("EC")
        kpg.initialize(256)
        keyPair = kpg.generateKeyPair()

        keyAgreement = KeyAgreement.getInstance("ECDH")
        keyAgreement.init(keyPair.private)
    }

    fun computeSharedSecret(otherPublicKey: ByteArray) {
        val kf = KeyFactory.getInstance("EC")
        val pubKeySpec = X509EncodedKeySpec(otherPublicKey)
        val peerPublicKey = kf.generatePublic(pubKeySpec)
        keyAgreement.doPhase(peerPublicKey, true)
        sharedSecret = keyAgreement.generateSecret()
    }

    private fun generateAesKey(sharedSecret: ByteArray): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(sharedSecret).copyOf(16) // 128-bit AES key
    }
}
