package com.teamapi.palette.response

import kotlinx.serialization.Serializable
import org.springframework.http.ResponseEntity

@Serializable
data class ErrorResponse(override val code: Int, override val message: String, val kind: String) : BaseResponse {
    companion object {
        fun of(code: ResponseCode, vararg formats: Any?): ResponseEntity<ErrorResponse> =
            ResponseEntity.status(code.statusCode)
                .body(ofRaw(code, *formats))

        fun ofRaw(code: ResponseCode, vararg formats: Any?): ErrorResponse =
            ErrorResponse(code.statusCode.value(), code.message.format(*formats), code.getName())
    }
}
