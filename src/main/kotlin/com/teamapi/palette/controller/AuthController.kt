package com.teamapi.palette.controller

import com.teamapi.palette.dto.auth.LoginRequest
import com.teamapi.palette.dto.auth.RegisterRequest
import com.teamapi.palette.dto.user.PasswordUpdateRequest
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

    @GetMapping("/session")
    fun updateCurrentSession(session: WebSession) = session
        .changeSessionId()
        .thenReturn(Response.ok("세션 갱신 성공"))

    @PatchMapping("/password")
    fun passwordUpdate(@RequestBody request: PasswordUpdateRequest) = authService
        .passwordUpdate(request)
        .thenReturn(Response.ok("비밀번호 변경 성공. 다시 로그인 해 주세요."))

    @DeleteMapping("/resign")
    fun resign(webSession: WebSession) = authService
        .resign(webSession)
        .thenReturn(Response.ok("유저 삭제 성공"))
}
