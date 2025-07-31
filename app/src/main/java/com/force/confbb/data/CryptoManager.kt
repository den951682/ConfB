package com.force.confbb.data

import android.util.Log
import com.force.confbb.util.TAG
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class CryptoManager(
    passphrase: String,
) {
    private val key = deriveKeyFromPassphrase(passphrase, byteArrayOf(0, 1, 2, 3, 4, 5, 6, 7))

    fun deriveKeyFromPassphrase(passphrase: String, salt: ByteArray): SecretKey {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        //todo set iterations to 100000. Встановлено 10000 так як esp32 дуже довго генерує ключ, варто ключ генерувати поза esp32 і передавати на неї через nfc або usb
        val spec = PBEKeySpec(passphrase.toCharArray(), salt, 10000, 256)
        val tmp = factory.generateSecret(spec)
        Log.d(TAG, "Pass ${passphrase}:  generated key " + tmp.encoded.joinToString(" ") { "%02X".format(it) })
        return SecretKeySpec(tmp.encoded, "AES")
    }

    fun encryptDataWhole(data: ByteArray): ByteArray {
        return encryptData(data, key).let { (iv, enc) -> iv + enc }
    }

    fun encryptData(data: ByteArray): Pair<ByteArray, ByteArray> {
        return encryptData(data, key)
    }

    private fun encryptData(data: ByteArray, secretKey: SecretKey): Pair<ByteArray, ByteArray> {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val encrypted = cipher.doFinal(data)
        return Pair(iv, encrypted)
    }

    fun decryptData(encrypted: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, encrypted.take(12).toByteArray())
        cipher.init(Cipher.DECRYPT_MODE, key, spec)
        val decrypted = cipher.doFinal(encrypted.drop(12).toByteArray())
        return decrypted
    }

    private fun decryptData(iv: ByteArray, encrypted: ByteArray, secretKey: SecretKey): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        val decrypted = cipher.doFinal(encrypted)
        return decrypted
    }
}

//todo add support for salt
//todo after generation of key, encrypt it with other key and save it to db. Don`t save passphrase. This another key is saved in Cryptostorage with alias
