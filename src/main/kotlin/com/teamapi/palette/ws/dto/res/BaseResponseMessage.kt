package com.teamapi.palette.ws.dto.res

data class BaseResponseMessage<T>(
    val type: MessageType,
    val data: T
)
