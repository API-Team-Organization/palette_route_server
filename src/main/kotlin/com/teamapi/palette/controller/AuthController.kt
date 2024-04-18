package com.teamapi.palette.controller

import com.teamapi.palette.dto.auth.RegisterRequest
import com.teamapi.palette.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/auth")
class AuthController(
    val userService: UserService
) {
    @PostMapping("/register")
    fun register(@RequestBody request: Mono<RegisterRequest>): Mono<ResponseEntity<Unit>> {
        return request
            .flatMap { userService.register(it) }
            .thenReturn(ResponseEntity.ok().build())
    }
}