package com.sd.laborator.business.helpers

import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object AesHelper {

    // cheie is initialization vector hardcodate
    private val SECRET_KEY = "FamilyExpKey1234".toByteArray(Charsets.UTF_8)
    private val IV          = "InitVector123456".toByteArray(Charsets.UTF_8)

    private fun cipher(mode: Int): Cipher {
        val keySpec = SecretKeySpec(SECRET_KEY, "AES")
        val ivSpec  = IvParameterSpec(IV)
        return Cipher.getInstance("AES/CBC/PKCS5Padding").also {
            it.init(mode, keySpec, ivSpec)
        }
    }

    fun encrypt(plaintext: String): String {
        val encrypted = cipher(Cipher.ENCRYPT_MODE).doFinal(plaintext.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(encrypted)
    }

    fun decrypt(ciphertext: String): String {
        val decoded   = Base64.getDecoder().decode(ciphertext)
        val decrypted = cipher(Cipher.DECRYPT_MODE).doFinal(decoded)
        return String(decrypted, Charsets.UTF_8)
    }
}