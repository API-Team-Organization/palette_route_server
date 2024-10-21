package com.teamapi.palette.dto.response.user

import com.teamapi.palette.entity.User
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.serialization.Serializable

@Serializable
    data class UserResponse(
        val id: Long,
        val name: String,
        val email: String,
        val birthDate: LocalDate
    ) {
        companion object {
            fun from(user: User): UserResponse {
                return UserResponse(user.id!!, user.username, user.email, user.birthDate.toKotlinLocalDate())
            }
        }
    }