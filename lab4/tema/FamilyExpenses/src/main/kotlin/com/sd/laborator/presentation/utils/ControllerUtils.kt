package com.sd.laborator.presentation.utils

import com.sd.laborator.business.models.MyCustomError
import com.sd.laborator.business.models.MyCustomResponse
import org.springframework.http.ResponseEntity

object ControllerUtils {

    fun makeResponse(response: MyCustomResponse<Any>): ResponseEntity<Any> {
        return if (response.successfulOperation) {
            ResponseEntity.status(response.code).body(response.data)
        } else {
            val err = MyCustomError(response.code, response.error, response.message)
            ResponseEntity.status(response.code).body(err)
        }
    }
}