package com.teamapi.palette.response

import kotlinx.serialization.Serializable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

@Serializable
class ResponseBody<T : Any>(
    override val code: Int,
    override val message: String,
    val data: T,
) : BaseResponse {
    companion object {
        fun <T : Any> of(code: HttpStatus, message: String, data: T): ResponseEntity<ResponseBody<T>> =
            ResponseEntity.status(code).body(ResponseBody(code.value(), message, data))

        fun <T : Any> ok(message: String, data: T) = of(HttpStatus.OK, message, data)
        fun <T : Any> created(message: String, data: T) = of(HttpStatus.CREATED, message, data)
        fun <T : Any> noContent(message: String, data: T) = of(HttpStatus.NO_CONTENT, message, data)
    }
}
