package com.teamapi.palette.dto.chat

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class AzureExceptionResponse(
    val error: AzureError
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AzureError(
    val code: String,
    @JsonAlias("innererror", "inner_error")
    val innerError: AzureInnerError
) {
    data class AzureInnerError(
        val code: String
    )
}
