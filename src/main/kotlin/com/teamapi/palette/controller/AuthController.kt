package com.teamapi.palette.controller

import com.teamapi.palette.annotations.SwaggerRequireAuthorize
import com.teamapi.palette.dto.request.auth.EmailVerifyRequest
import com.teamapi.palette.dto.request.auth.LoginRequest
import com.teamapi.palette.dto.request.auth.RegisterRequest
import com.teamapi.palette.dto.request.user.PasswordUpdateRequest
import com.teamapi.palette.response.Response
import com.teamapi.palette.service.AuthService
import com.teamapi.palette.service.SessionHolder
import jakarta.validation.Valid
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.WebSession

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService,
    private val sessionHolder: SessionHolder
) {
    @PostMapping("/register")
    suspend fun register(@RequestBody @Valid request: RegisterRequest): ResponseEntity<Response> {
        authService.register(request)
        return Response.ok("회원가입 성공")
    }

    @PostMapping("/login")
    suspend fun login(@RequestBody @Valid request: LoginRequest): ResponseEntity<Response> {
        authService.authenticate(request.email, request.password)
        return Response.ok("로그인 성공")
    }

    @PostMapping("/resend")
    @SwaggerRequireAuthorize
    suspend fun resendCode(): ResponseEntity<Response> {
        authService.resendVerifyCode()
        return Response.ok("이메일 코드 재전송 성공")
    }

    @PostMapping("/verify")
    @SwaggerRequireAuthorize
    suspend fun verify(@RequestBody @Valid request: EmailVerifyRequest): ResponseEntity<Response> {
        authService.verifyEmail(request.code)
        return Response.ok("이메일 인증 성공")
    }

    @PostMapping("/logout")
    @SwaggerRequireAuthorize
    suspend fun logout(): ResponseEntity<Response> {
        sessionHolder.getWebSession().invalidate().awaitSingleOrNull()
        return Response.ok("로그아웃 성공")
    }

    @GetMapping("/session")
    @SwaggerRequireAuthorize
    suspend fun updateCurrentSession() = Response.ok("세션 갱신 성공")

    @PatchMapping("/password")
    @SwaggerRequireAuthorize
    suspend fun passwordUpdate(@RequestBody @Valid request: PasswordUpdateRequest): ResponseEntity<Response> {
        authService.passwordUpdate(request)
        return Response.ok("비밀번호 변경 성공. 다시 로그인 해 주세요.")
    }

    @DeleteMapping("/resign")
    @SwaggerRequireAuthorize
    suspend fun resign(webSession: WebSession): ResponseEntity<Response> {
        authService.resign()
        return Response.ok("회원탈퇴 성공")
    }
}
