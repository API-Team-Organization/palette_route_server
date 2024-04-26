package com.teamapi.palette.service

import com.teamapi.palette.dto.user.UserResponse
import com.teamapi.palette.repository.UserRepository
import com.teamapi.palette.response.ErrorCode
import com.teamapi.palette.response.exception.CustomException
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@Service
class UserService(
    private val userRepository: UserRepository,
    private val sessionHolder: SessionHolder
) {
    fun me(): Mono<UserResponse> {
        return sessionHolder
            .current()
            .flatMap {
                Mono.justOrEmpty(it.getAttribute<Long>("user"))
            }
            .switchIfEmpty { Mono.error(CustomException(ErrorCode.INVALID_SESSION)) }
            .flatMap {
                userRepository.findById(it)
                    .map { UserResponse.from(it) }
            }
    }
}