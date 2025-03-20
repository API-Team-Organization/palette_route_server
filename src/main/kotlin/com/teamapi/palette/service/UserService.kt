package com.teamapi.palette.service

import com.teamapi.palette.dto.request.user.UserUpdateRequest
import com.teamapi.palette.dto.response.user.UserResponse
import com.teamapi.palette.repository.user.UserRepository
import kotlinx.datetime.toJavaLocalDate
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val sessionHolder: SessionHolder
) {
    suspend fun me(): UserResponse = sessionHolder.me(userRepository).let { UserResponse.from(it) }

    suspend fun update(request: UserUpdateRequest) {
        val me = sessionHolder.me(userRepository)
        userRepository.modify(
            me.copy(
                username = request.username ?: me.username,
                birthDate = request.birthDate ?: me.birthDate
            )
        )
    }
}
