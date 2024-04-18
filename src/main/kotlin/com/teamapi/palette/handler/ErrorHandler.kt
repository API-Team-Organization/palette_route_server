package com.teamapi.palette.handler

import com.teamapi.palette.response.ErrorCode
import com.teamapi.palette.response.Response
import com.teamapi.palette.response.exception.CustomException
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@Suppress("unused")
@RestControllerAdvice
class ErrorHandler {
    @ExceptionHandler(value = [CustomException::class])
    private fun customExceptionHandler(custom: CustomException): ResponseEntity<Response> =
        Response.of(custom.responseCode.statusCode, custom.responseCode.message)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    private fun handleValidException(e: MethodArgumentNotValidException) =
        Response.of(e.statusCode, e.bindingResult.allErrors.joinToString(", ") { (it as FieldError).field })

    @ExceptionHandler(MissingServletRequestParameterException::class)
    private fun handleMissingServletRequestParameterException(e: MissingServletRequestParameterException) =
        Response.of(ErrorCode.INVALID_PARAMETER.statusCode, ErrorCode.INVALID_PARAMETER.message)

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    private fun handleHttpRequestMethodNotSupportedException(e: HttpRequestMethodNotSupportedException) =
        Response.of(ErrorCode.METHOD_NOT_ALLOWED.statusCode, ErrorCode.METHOD_NOT_ALLOWED.message)

    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    private fun handleHttpMediaTypeNotSupportedException(e: HttpMediaTypeNotSupportedException) =
        Response.of(ErrorCode.MEDIA_TYPE_NOT_SUPPORTED.statusCode, ErrorCode.MEDIA_TYPE_NOT_SUPPORTED.message)

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    private fun handleMethodArgumentTypeMismatchException(e: MethodArgumentTypeMismatchException) =
        Response.of(ErrorCode.INVALID_PARAMETER.statusCode, ErrorCode.INVALID_PARAMETER.message)
}