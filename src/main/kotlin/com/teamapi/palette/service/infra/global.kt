package com.teamapi.palette.service.infra

import com.azure.core.exception.HttpResponseException
import com.teamapi.palette.dto.request.chat.AzureExceptionResponse
import com.teamapi.palette.response.ErrorCode
import com.teamapi.palette.response.exception.CustomException
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono

private val log = LoggerFactory.getLogger("OpenAIService")

fun <T> Mono<T>.handleAzureError(mapper: Json) = onErrorMap(HttpResponseException::class.java) {
    try {
        if (mapper.decodeFromString<AzureExceptionResponse>(it.value.toString()).error.innerError.code != "ResponsibleAIPolicyViolation")
            throw it
        CustomException(ErrorCode.CHAT_FILTERED)
    } catch (e: Throwable) {
        log.error("Error on Azure: ", it)
        log.error("Body: {}", it.value)
        CustomException(ErrorCode.INTERNAL_SERVER_EXCEPTION)
    }
}
