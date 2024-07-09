package com.teamapi.palette.dto.chat

data class CreateChatRequest(
    val message: String,
    val roomId: Long,
)
