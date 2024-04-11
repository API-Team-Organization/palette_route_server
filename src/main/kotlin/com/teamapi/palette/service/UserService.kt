package com.teamapi.palette.service

import com.teamapi.palette.dto.auth.RegisterRequest
import com.teamapi.palette.repository.UserRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class UserService(
    private val userRepository: UserRepository
) {
    fun register(request: RegisterRequest): Mono<Void> {
        return userRepository.save(request.toEntity())
            .then()
    }
}