package com.teamapi.palette.controller

import com.teamapi.palette.dto.auth.LoginRequest
import com.teamapi.palette.dto.auth.RegisterRequest
import com.teamapi.palette.response.Response
import com.teamapi.palette.response.ResponseBody
import com.teamapi.palette.service.AuthService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/auth")
class AuthController(
    val authService: AuthService
) {
    @PostMapping("/register")
    fun register(@RequestBody request: Mono<RegisterRequest>) = request
        .flatMap { authService.register(it) }
        .thenReturn(Response.ok("회원가입 성공"))

    @PostMapping("/login")
    fun login(@RequestBody request: Mono<LoginRequest>) = request
        .flatMap { authService.login(it) }
        .map { ResponseBody.ok("로그인 성공", it) }
}