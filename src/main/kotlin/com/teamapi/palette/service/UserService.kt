package com.teamapi.palette.service

import com.teamapi.palette.dto.user.UpdateRequest
import com.teamapi.palette.dto.user.UserResponse
import com.teamapi.palette.repository.UserRepository
import com.teamapi.palette.util.findUser
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class UserService(
    private val userRepository: UserRepository,
    private val sessionHolder: SessionHolder
) {
    fun me(): Mono<UserResponse> = sessionHolder
        .me()
        .findUser(userRepository)
        .map { UserResponse.from(it) }

    fun update(request: UpdateRequest): Mono<Void> {
        return sessionHolder
            .me()
            .findUser(userRepository)
            .flatMap {
                userRepository.save(
                    it.copy(
                        username = request.username ?: it.username,
                        birthDate = request.birthDate ?: it.birthDate
                    )
                )
            }
            .then()
    }
}
