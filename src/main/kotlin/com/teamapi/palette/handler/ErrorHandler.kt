package com.teamapi.palette.handler

import com.teamapi.palette.response.GlobalResponseCode
import com.teamapi.palette.response.Response
import com.teamapi.palette.response.exception.CustomException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ErrorHandler {
    @ExceptionHandler(value = [CustomException::class])
    fun customExceptionHandler(custom: CustomException): ResponseEntity<Response>
        = Response.of(custom.responseCode.statusCode, custom.responseCode.message)

    @ExceptionHandler(value = [Exception::class])
    fun defaultExceptionHandler(exception: Exception): ResponseEntity<Response> {
        exception.printStackTrace()
        return Response.of(
            GlobalResponseCode.INTERNAL_SERVER_EXCEPTION.statusCode,
            GlobalResponseCode.INTERNAL_SERVER_EXCEPTION.message
        )
    }
}