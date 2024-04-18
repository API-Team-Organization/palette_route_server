package com.teamapi.palette.service

import com.teamapi.palette.dto.auth.RegisterRequest
import com.teamapi.palette.repository.UserRepository
import com.teamapi.palette.response.GlobalResponseCode
import com.teamapi.palette.response.exception.CustomException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.adapter.rxjava.toSingle
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.extra.bool.not

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    fun register(request: RegisterRequest): Mono<Void> {
        return userRepository.existsByEmail(request.email)
            .flatMap {
                if (it) Mono.error(CustomException(GlobalResponseCode.USER_ALREADY_EXISTS))
                else userRepository.save(request.toEntity(passwordEncoder)).then()
            }
    }
}