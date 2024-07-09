package com.teamapi.palette.dto.chat

import java.time.LocalDateTime

data class ChatResponse(
    val id: Long?,
    val message: String,
    val datetime: LocalDateTime,
    val roomId: Long,
    val userId: Long,
    val isAi: Boolean,
    val resource: String
)
