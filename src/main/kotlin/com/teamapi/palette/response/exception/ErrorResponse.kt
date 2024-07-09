package com.teamapi.palette.response.exception

import com.teamapi.palette.response.Response
import com.teamapi.palette.response.ResponseCode
import org.springframework.http.ResponseEntity

class ErrorResponse(code: Int, message: String, @Suppress("unused") val kind: String) : Response(code, message) {
    companion object {
        fun of(code: ResponseCode, vararg formats: Any?): ResponseEntity<ErrorResponse> =
            ResponseEntity.status(code.statusCode)
                .body(ErrorResponse(code.statusCode.value(), code.message.format(*formats), code.getName()))
    }
}
