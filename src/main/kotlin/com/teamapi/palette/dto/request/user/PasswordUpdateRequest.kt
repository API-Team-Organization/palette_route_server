package com.teamapi.palette.dto.request.user

import jakarta.validation.constraints.NotBlank
import kotlinx.serialization.Serializable

@Serializable
data class PasswordUpdateRequest(
    @field:NotBlank
    val beforePassword: String,
    @field:NotBlank
    val afterPassword: String
)
