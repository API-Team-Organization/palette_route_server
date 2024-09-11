package com.teamapi.palette.service

import com.teamapi.palette.dto.user.UpdateRequest
import com.teamapi.palette.dto.user.UserResponse
import com.teamapi.palette.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val sessionHolder: SessionHolder
) {
    suspend fun me(): UserResponse = sessionHolder.me(userRepository).let { UserResponse.from(it) }

    suspend fun update(request: UpdateRequest) {
        val me = sessionHolder.me(userRepository)
        userRepository.save(
            me.copy(
                username = request.username ?: me.username,
                birthDate = request.birthDate ?: me.birthDate
            )
        )
    }
}
