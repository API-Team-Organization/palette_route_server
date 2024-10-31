package com.teamapi.palette.handler

import com.teamapi.palette.response.ErrorCode
import com.teamapi.palette.response.exception.CustomException
import com.teamapi.palette.response.ErrorResponse
import com.teamapi.palette.util.ExceptionReporter
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.SerializationException
import org.springframework.core.codec.EncodingException
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.server.MethodNotAllowedException
import org.springframework.web.server.ServerWebInputException
import org.springframework.web.server.UnsupportedMediaTypeStatusException

@Suppress("unused")
@RestControllerAdvice
class ErrorHandler(
    val reporter: ExceptionReporter
) {
    @ExceptionHandler(value = [CustomException::class])
    private fun customExceptionHandler(custom: CustomException): ResponseEntity<ErrorResponse> =
        ErrorResponse.of(custom.responseCode, *custom.formats)

    @ExceptionHandler(WebExchangeBindException::class)
    private fun handleValidException(e: WebExchangeBindException) =
        ErrorResponse.of(ErrorCode.ISSUE_ON_REQUEST_BODY, e.bindingResult.allErrors.joinToString(", ") { "Field '${(it as FieldError).field}' ${it.defaultMessage}" })

    @ExceptionHandler(MethodNotAllowedException::class)
    private fun handleMethodNotAllowedException(e: MethodNotAllowedException) =
        ErrorResponse.of(ErrorCode.METHOD_NOT_ALLOWED, e.httpMethod, e.supportedMethods.joinToString(", ") { it.name() })

    @ExceptionHandler(UnsupportedMediaTypeStatusException::class)
    private fun handleUnsupportedMediaTypeStatusException(e: UnsupportedMediaTypeStatusException) =
        ErrorResponse.of(ErrorCode.MEDIA_TYPE_NOT_SUPPORTED, e.contentType?.toString(), e.supportedMediaTypes.joinToString(", "))

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    private fun handleMethodArgumentTypeMismatchException(e: MethodArgumentTypeMismatchException) =
        ErrorResponse.of(ErrorCode.INVALID_PARAMETER, e.parameter.method?.name)

    @ExceptionHandler
    @OptIn(ExperimentalSerializationApi::class)
    private fun handleMissingFieldException(e: MissingFieldException) =
        ErrorResponse.of(ErrorCode.INVALID_PARAMETER, e.missingFields.joinToString(", "))

    @ExceptionHandler
    private fun handleJsonDecodingException(e: SerializationException) =
        ErrorResponse.of(ErrorCode.ISSUE_ON_REQUEST_BODY, e.message)

    @ExceptionHandler
    private fun handleEncodingException(e: EncodingException): ResponseEntity<ErrorResponse> {
        e.printStackTrace() // Possibly no @Serialization annotation
        reporter.doReport(e)
        return ErrorResponse.of(ErrorCode.INTERNAL_SERVER_EXCEPTION)
    }

    @ExceptionHandler(ServerWebInputException::class)
    private fun handleServerWebInputException(e: ServerWebInputException): ResponseEntity<ErrorResponse> {
        e.printStackTrace()
        return ErrorResponse.of(ErrorCode.ISSUE_ON_REQUEST_BODY, e.reason)
    }
}
