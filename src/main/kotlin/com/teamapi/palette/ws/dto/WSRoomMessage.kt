package com.teamapi.palette.ws.dto

import com.teamapi.palette.ws.dto.res.BaseResponseMessage
import kotlinx.serialization.Serializable

@Serializable
data class WSRoomMessage(
    val roomId: Long,
    val message: BaseResponseMessage, // change to nullable if needed
)
