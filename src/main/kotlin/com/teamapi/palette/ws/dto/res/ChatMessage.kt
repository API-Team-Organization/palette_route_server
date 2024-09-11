package com.teamapi.palette.ws.dto.res

import com.teamapi.palette.dto.chat.ChatResponse
import com.teamapi.palette.ws.dto.RoomAction

data class ChatMessage(
    val action: RoomAction,
    val message: ChatResponse?
) : BaseResponseMessage(MessageType.NEW_CHAT)
