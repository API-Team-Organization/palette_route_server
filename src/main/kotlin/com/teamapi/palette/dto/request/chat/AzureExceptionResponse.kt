package com.teamapi.palette.dto.request.chat

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class AzureExceptionResponse(
    val error: AzureError
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class AzureError(
    val code: String,
    @JsonNames("innererror", "inner_error")
    val innerError: AzureInnerError
) {
    @Serializable
    data class AzureInnerError(
        val code: String
    )
}
