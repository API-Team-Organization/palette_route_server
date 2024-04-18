package com.teamapi.palette.response

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

open class Response(
    open val code: Int,
    open val message: String,
) {
    companion object {
        fun of(code: HttpStatus, message: String): ResponseEntity<Response>
            = ResponseEntity.status(code).body(Response(code.value(), message))
        fun ok(message: String) = of(HttpStatus.OK, message)
        fun created(message: String) = of(HttpStatus.CREATED, message)
        fun noContent(message: String) = of(HttpStatus.NO_CONTENT, message)
    }
}