package com.sd.laborator.presentation.controllers

import com.sd.laborator.business.interfaces.ICryptoService
import com.sd.laborator.business.models.EncryptRequest
import com.sd.laborator.presentation.utils.ControllerUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/crypto")
class CryptoController {

    @Autowired
    private lateinit var cryptoService: ICryptoService

    @PostMapping("/encrypt")
    fun encrypt(@RequestBody req: EncryptRequest): ResponseEntity<Any> =
        ControllerUtils.makeResponse(cryptoService.encrypt(req.plaintext))

    @PostMapping("/decrypt")
    fun decrypt(@RequestBody req: EncryptRequest): ResponseEntity<Any> =
        ControllerUtils.makeResponse(cryptoService.decrypt(req.plaintext))
}