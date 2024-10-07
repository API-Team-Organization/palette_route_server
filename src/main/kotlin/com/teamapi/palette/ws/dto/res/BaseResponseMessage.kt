package com.teamapi.palette.ws.dto.res

import kotlinx.serialization.Serializable

@Serializable
sealed interface BaseResponseMessage {
    val type: MessageType
}
