package com.teamapi.palette.ws.dto.res

import com.teamapi.palette.response.ErrorCode
import kotlinx.serialization.Serializable

@Serializable
data class ErrorMessage(val kind: String, val message: String) : BaseResponseMessage {
    override val type = MessageType.ERROR

    companion object {
        fun of(e: ErrorCode) = ErrorMessage(kind = e.name, message = e.message)
    }
}
