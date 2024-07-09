package com.teamapi.palette.dto.chat

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class AzureExceptionResponse(
    val error: AzureError
)

data class AzureError(
    val message: String,
    val code: String,
    val status: Int
)
