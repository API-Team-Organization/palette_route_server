package com.teamapi.palette.dto.chat

import java.time.Instant

data class ChatResponse(
    val id: Long?,
    val message: String,
    val datetime: Instant,
    val roomId: Long,
    val userId: Long,
    val isAi: Boolean
)
