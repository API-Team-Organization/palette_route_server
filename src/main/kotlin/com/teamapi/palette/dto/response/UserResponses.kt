package com.teamapi.palette.dto.response

import com.teamapi.palette.entity.User
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.serialization.Serializable

@Serializable
sealed interface UserResponses : Responses {
    @Serializable
    data class UserResponse(
        val id: Long,
        val name: String,
        val email: String,
        val birthDate: LocalDate
    ) : UserResponses {
        companion object {
            fun from(user: User): UserResponse {
                return UserResponse(user.id!!, user.username, user.email, user.birthDate.toKotlinLocalDate())
            }
        }
    }
}
