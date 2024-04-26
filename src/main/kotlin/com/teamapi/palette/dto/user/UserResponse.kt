package com.teamapi.palette.dto.user

import com.teamapi.palette.entity.User
import java.time.LocalDate

data class UserResponse(
    val id: Long,
    val name: String,
    val email: String,
    val birthDate: LocalDate
) {
    companion object {
        fun from(user: User): UserResponse {
            return UserResponse(user.id!!, user.username, user.email, user.birthDate)
        }
    }
}