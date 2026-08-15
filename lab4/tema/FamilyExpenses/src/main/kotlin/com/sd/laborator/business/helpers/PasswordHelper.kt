package com.sd.laborator.business.helpers

import java.security.MessageDigest

object PasswordHelper {

    fun hash(username: String, password: String): String {
        val input  = (username + password).toByteArray(Charsets.UTF_8)
        val digest = MessageDigest.getInstance("SHA-256").digest(input)
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun matches(username: String, rawPassword: String, storedHash: String): Boolean =
        hash(username, rawPassword) == storedHash
}