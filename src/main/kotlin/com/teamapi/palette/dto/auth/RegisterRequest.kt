package com.teamapi.palette.dto.auth

import com.teamapi.palette.entity.User
import java.util.Date

data class RegisterRequest(
    val username: String,
    val password: String,
    val birthDate: Date,
    val email: String,
) {
    fun toEntity(): User = User(
        email,
        password,
        username,
        birthDate
    )

    companion object {
        fun fromEntity(dto: User): RegisterRequest =
            RegisterRequest(dto.username, dto.password, dto.birthDate, dto.email)
    }
}