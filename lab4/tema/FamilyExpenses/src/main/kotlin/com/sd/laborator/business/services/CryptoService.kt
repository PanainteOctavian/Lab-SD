package com.sd.laborator.business.services

import com.sd.laborator.business.helpers.AesHelper
import com.sd.laborator.business.interfaces.ICryptoService
import com.sd.laborator.business.models.MyCustomResponse
import org.springframework.stereotype.Service

@Service
class CryptoService : ICryptoService {

    override fun encrypt(plaintext: String): MyCustomResponse<Any> {
        return try {
            if (plaintext.isBlank())
                return MyCustomResponse(
                    successfulOperation = false, code = 400, data = Unit,
                    error = "Bad Request", message = "Textul nu poate fi gol."
                )
            val ciphertext = AesHelper.encrypt(plaintext)
            MyCustomResponse(
                successfulOperation = true,
                code = 200,
                data = mapOf("ciphertext" to ciphertext, "algorithm" to "AES-128/CBC/PKCS5Padding")
            )
        } catch (e: Exception) {
            MyCustomResponse(successfulOperation = false, code = 500, data = Unit, error = e.message)
        }
    }

    override fun decrypt(ciphertext: String): MyCustomResponse<Any> {
        return try {
            if (ciphertext.isBlank())
                return MyCustomResponse(
                    successfulOperation = false, code = 400, data = Unit,
                    error = "Bad Request", message = "Ciphertext-ul nu poate fi gol."
                )
            val plaintext = AesHelper.decrypt(ciphertext)
            MyCustomResponse(
                successfulOperation = true,
                code = 200,
                data = mapOf("plaintext" to plaintext)
            )
        } catch (e: Exception) {
            MyCustomResponse(
                successfulOperation = false, code = 400, data = Unit,
                error = "Bad Request", message = "Ciphertext invalid sau cheie greșită."
            )
        }
    }
}