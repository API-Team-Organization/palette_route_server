package com.teamapi.palette.dto.request.user

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class UserUpdateRequest (
    val username: String?,
    val birthDate: LocalDate?
)
