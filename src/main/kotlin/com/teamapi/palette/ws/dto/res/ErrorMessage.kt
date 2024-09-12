package com.teamapi.palette.ws.dto.res

import com.teamapi.palette.response.ErrorCode

data class ErrorMessage(
    val kind: String,
    val message: String,
) : BaseResponseMessage(MessageType.ERROR) {
    companion object {
        fun of(e: ErrorCode) = ErrorMessage(kind = e.name, message = e.message)
    }
}
