package com.force.crypto

import java.math.BigInteger
import java.security.AlgorithmParameters
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.PublicKey
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import java.security.spec.ECParameterSpec
import java.security.spec.ECPoint
import java.security.spec.ECPublicKeySpec
import javax.crypto.KeyAgreement

class CryptoEcdh {
    private lateinit var keyPair: KeyPair
    private lateinit var keyAgreement: KeyAgreement
    private lateinit var sharedSecret: ByteArray
    private lateinit var cryptoAes: CryptoAes

    init {
        generateKeyPair()
    }

    /*
    fun getPublicKey(): ByteArray {
        return keyPair.public.encoded
    }
     */

    fun getPublicKey(): ByteArray {
        val pub = keyPair.public as ECPublicKey
        val w = pub.w

        val x = w.affineX.toByteArray().stripLeadingZeros(32)
        val y = w.affineY.toByteArray().stripLeadingZeros(32)

        // 0x04 + X(32) + Y(32)
        return byteArrayOf(0x04) + x + y
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
        /*val kf = KeyFactory.getInstance("EC")
        val pubKeySpec = X509EncodedKeySpec(otherPublicKey)
        val peerPublicKey = kf.generatePublic(pubKeySpec)*/
        val peerPublicKey = loadUncompressedPoint(otherPublicKey)
        keyAgreement.doPhase(peerPublicKey, true)
        sharedSecret = keyAgreement.generateSecret()
    }

    private fun generateAesKey(sharedSecret: ByteArray): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(sharedSecret).copyOf(16) // 128-bit AES key
    }

    private fun loadUncompressedPoint(otherPubKey: ByteArray): PublicKey {
        require(otherPubKey[0] == 0x04.toByte()) { "Only uncompressed form supported" }

        val x = otherPubKey.copyOfRange(1, 33)
        val y = otherPubKey.copyOfRange(33, 65)

        val point = ECPoint(BigInteger(1, x), BigInteger(1, y))

        val params = AlgorithmParameters.getInstance("EC")
        params.init(ECGenParameterSpec("secp256r1"))
        val ecSpec = params.getParameterSpec(ECParameterSpec::class.java)

        val pubKeySpec = ECPublicKeySpec(point, ecSpec)
        return KeyFactory.getInstance("EC").generatePublic(pubKeySpec)
    }

    private fun ByteArray.stripLeadingZeros(size: Int): ByteArray {
        var arr = this
        if (arr.size > size) {
            arr = arr.copyOfRange(arr.size - size, arr.size)
        }
        if (arr.size < size) {
            val tmp = ByteArray(size)
            System.arraycopy(arr, 0, tmp, size - arr.size, arr.size)
            arr = tmp
        }
        return arr
    }
}
