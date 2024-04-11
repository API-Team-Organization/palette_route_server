package com.teamapi.palette.service

import com.teamapi.palette.entity.User
import com.teamapi.palette.repository.UserRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class UserService(
    val userRepository: UserRepository
) {
    fun register(): Mono<User> {
        TODO()
    }
}