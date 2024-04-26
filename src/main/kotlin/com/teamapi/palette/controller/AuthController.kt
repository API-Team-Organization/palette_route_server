package com.teamapi.palette.controller

import com.teamapi.palette.dto.auth.LoginRequest
import com.teamapi.palette.dto.auth.RegisterRequest
import com.teamapi.palette.response.Response
import com.teamapi.palette.service.AuthService
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.WebSession
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService
) {
    @PostMapping("/register")
    fun register(@RequestBody request: Mono<RegisterRequest>) = request
        .flatMap { authService.register(it) }
        .thenReturn(Response.ok("회원가입 성공"))

    @PostMapping("/login")
    fun login(@RequestBody request: Mono<LoginRequest>) = request
        .flatMap { authService.login(it) }
        .thenReturn(Response.ok("로그인 성공"))

    @PostMapping("/logout")
    fun logout(webSession: WebSession) = webSession
        .invalidate()
        .thenReturn(Response.ok("로그아웃 성공"))
}