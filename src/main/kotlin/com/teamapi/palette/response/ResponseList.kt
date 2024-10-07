package com.teamapi.palette.response

import com.teamapi.palette.dto.response.Responses
import kotlinx.serialization.Serializable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

@Serializable
class ResponseList(
    override val code: Int,
    override val message: String,
    val data: List<Responses>,
) : BaseResponse {
    companion object {
        fun of(code: HttpStatus, message: String, data: List<Responses>): ResponseEntity<ResponseList> =
            ResponseEntity.status(code).body(ResponseList(code.value(), message, data))

        fun ok(message: String, data: List<Responses>) = of(HttpStatus.OK, message, data)
    }
}
