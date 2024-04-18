package com.teamapi.palette.dto.auth

import com.teamapi.palette.entity.User
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDate

data class RegisterRequest(
    val username: String,
    val password: String,
    val birthDate: LocalDate,
    val email: String,
) {
    fun toEntity(encoder: PasswordEncoder): User = User(
        email,
        encoder.encode(password),
        username,
        birthDate
    )

    companion object {
        fun fromEntity(dto: User): RegisterRequest =
            RegisterRequest(dto.username, dto.password, dto.birthDate, dto.email)
    }
}