package com.teamapi.palette.response

import kotlinx.serialization.Serializable
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity

@Serializable
data class Response(override val code: Int, override val message: String) : BaseResponse {
    companion object {
        fun of(code: HttpStatusCode, message: String): ResponseEntity<Response> =
            ResponseEntity.status(code).body(Response(code.value(), message))

        fun ok(message: String) = of(HttpStatus.OK, message)
        fun created(message: String) = of(HttpStatus.CREATED, message)
        fun noContent(message: String) = of(HttpStatus.NO_CONTENT, message)
    }
}
