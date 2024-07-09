package com.teamapi.palette.dto.auth

import com.teamapi.palette.entity.User
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDate

data class RegisterRequest(
    @field:NotBlank
    val username: String,
    @field:NotBlank
    val password: String,
    val birthDate: LocalDate,
    @field:NotBlank
    @field:Email
    val email: String,
) {
    fun toEntity(encoder: PasswordEncoder): User = User(
        email,
        encoder.encode(password),
        username,
        birthDate
    )
}
