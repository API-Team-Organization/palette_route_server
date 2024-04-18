package com.teamapi.palette.service

import com.teamapi.palette.dto.auth.LoginRequest
import com.teamapi.palette.dto.auth.RegisterRequest
import com.teamapi.palette.repository.UserRepository
import com.teamapi.palette.response.GlobalResponseCode
import com.teamapi.palette.response.exception.CustomException
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisOperations
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val factory: ReactiveRedisConnectionFactory,
    private val redisOperations: ReactiveRedisOperations<String, String>
) {
    fun register(request: RegisterRequest): Mono<Void> {
        return userRepository.existsByEmail(request.email)
            .flatMap {
                if (it) Mono.error(CustomException(GlobalResponseCode.USER_ALREADY_EXISTS))
                else userRepository.save(request.toEntity(passwordEncoder)).then()
            }
    }

    fun login(
        request: LoginRequest
    ): Mono<String> {
        val uuid = UUID.randomUUID()
        return userRepository.findByEmail(request.email)
            .switchIfEmpty { Mono.error(CustomException(GlobalResponseCode.INTERNAL_SERVER_EXCEPTION)) }
            .flatMap { user ->
                if (passwordEncoder.matches(request.password, user.password)) {
                    factory.reactiveConnection
                        .serverCommands()
                        .flushAll()
                        .then(
                            redisOperations
                                .opsForSet()
                                .add(uuid.toString())
                                .flatMap { Mono.just(uuid.toString()) }
                        )
                } else {
                    Mono.error(CustomException(GlobalResponseCode.INTERNAL_SERVER_EXCEPTION))
                }
            }
    }
}