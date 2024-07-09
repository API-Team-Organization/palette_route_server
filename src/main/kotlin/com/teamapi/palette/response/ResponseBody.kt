package com.teamapi.palette.response

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

class ResponseBody<T>(
    override val code: Int,
    override val message: String,
    val data: T,
    ) : Response(code, message) {
    companion object {
        fun <T> of(code: HttpStatus, message: String, data: T): ResponseEntity<ResponseBody<T>>
            = ResponseEntity.status(code).body(ResponseBody(code.value(), message, data))
        fun <T> ok(message: String, data: T) = of(HttpStatus.OK, message, data)
        fun <T> created(message: String, data: T) = of(HttpStatus.CREATED, message, data)
        fun <T> noContent(message: String, data: T) = of(HttpStatus.NO_CONTENT, message, data)
    }
}