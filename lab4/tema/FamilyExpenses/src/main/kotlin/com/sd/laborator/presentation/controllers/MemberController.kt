package com.sd.laborator.presentation.controllers

import com.sd.laborator.business.interfaces.IMemberService
import com.sd.laborator.business.models.LoginRequest
import com.sd.laborator.business.models.RegisterRequest
import com.sd.laborator.presentation.utils.ControllerUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/members")
class MemberController {

    @Autowired
    private lateinit var memberService: IMemberService

    @PostMapping("/register")
    fun register(@RequestBody req: RegisterRequest): ResponseEntity<Any> =
        ControllerUtils.makeResponse(memberService.register(req))

    @PostMapping("/login")
    fun login(@RequestBody req: LoginRequest): ResponseEntity<Any> =
        ControllerUtils.makeResponse(memberService.login(req))

    @GetMapping
    fun getAllMembers(): ResponseEntity<Any> =
        ControllerUtils.makeResponse(memberService.getAllMembers())

    @GetMapping("/{id}")
    fun getMember(@PathVariable id: Int): ResponseEntity<Any> =
        ControllerUtils.makeResponse(memberService.getMember(id))

    @DeleteMapping("/{id}")
    fun deleteMember(@PathVariable id: Int): ResponseEntity<Any> =
        ControllerUtils.makeResponse(memberService.deleteMember(id))
}