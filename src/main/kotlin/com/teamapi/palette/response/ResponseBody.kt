package com.teamapi.palette.response

import com.teamapi.palette.dto.response.Responses
import kotlinx.serialization.Serializable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

@Serializable
class ResponseBody(
    override val code: Int,
    override val message: String,
    val data: Responses,
) : BaseResponse {
    companion object {
        fun of(code: HttpStatus, message: String, data: Responses): ResponseEntity<ResponseBody> =
            ResponseEntity.status(code).body(ResponseBody(code.value(), message, data))

        fun ok(message: String, data: Responses) = of(HttpStatus.OK, message, data)
        fun created(message: String, data: Responses) = of(HttpStatus.CREATED, message, data)
        fun noContent(message: String, data: Responses) = of(HttpStatus.NO_CONTENT, message, data)
    }
}
