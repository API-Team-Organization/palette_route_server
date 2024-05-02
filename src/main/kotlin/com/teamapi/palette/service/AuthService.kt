package com.teamapi.palette.service

import com.teamapi.palette.dto.auth.LoginRequest
import com.teamapi.palette.dto.auth.RegisterRequest
import com.teamapi.palette.dto.user.PasswordUpdateRequest
import com.teamapi.palette.repository.UserRepository
import com.teamapi.palette.response.ErrorCode
import com.teamapi.palette.response.exception.CustomException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val sessionHolder: SessionHolder
) {
    fun register(request: RegisterRequest): Mono<Void> {
        return userRepository.existsByEmail(request.email)
            .flatMap {
                if (it) Mono.error(CustomException(ErrorCode.USER_ALREADY_EXISTS))
                else userRepository.save(request.toEntity(passwordEncoder)).then()
            }
    }

    fun login(
        request: LoginRequest,
    ): Mono<Void> {
        return userRepository.findByEmail(request.email)
            .switchIfEmpty { Mono.error(CustomException(ErrorCode.USER_NOT_FOUND)) }
            .filter { passwordEncoder.matches(request.password, it.password) }
            .switchIfEmpty { Mono.error(CustomException(ErrorCode.INVALID_PASSWORD)) }
            .flatMap {
                sessionHolder.current()
                    .flatMap { session ->
                        session.attributes["user"] = it.id
                        session.save()
                    }
            }
            .then()
    }

    fun passwordUpdate(request: PasswordUpdateRequest): Mono<Void> {
        return sessionHolder
            .me()
            .flatMap {
                userRepository.findById(it)
            }
            .filter {
                passwordEncoder.matches(request.beforePassword, it.password)
            }
            .switchIfEmpty {
                Mono.error(CustomException(ErrorCode.INVALID_PASSWORD))
            }
            .flatMap {
                userRepository.save(
                    it.copy(password = passwordEncoder.encode(request.afterPassword))
                )
            }
            .then()
    }
}