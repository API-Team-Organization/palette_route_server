package com.teamapi.palette.ws.dto

import com.teamapi.palette.dto.response.chat.ChatResponse
import kotlinx.serialization.Serializable

@Serializable
data class WSRoomMessage(
    val roomId: Long,
    val message: ChatResponse, // change to nullable if needed
)
