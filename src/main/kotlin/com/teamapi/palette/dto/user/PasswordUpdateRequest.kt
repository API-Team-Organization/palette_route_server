package com.teamapi.palette.dto.user

data class PasswordUpdateRequest(
    val beforePassword: String,
    val afterPassword: String
)
