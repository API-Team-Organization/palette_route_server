package com.teamapi.palette.dto.user

import jakarta.validation.constraints.NotBlank

data class PasswordUpdateRequest(
    @field:NotBlank
    val beforePassword: String,
    @field:NotBlank
    val afterPassword: String
)
