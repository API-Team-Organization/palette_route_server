package com.teamapi.palette.dto.request.auth

import kotlinx.serialization.Serializable

@Serializable
data class EmailVerifyRequest(
    val code: String
)
