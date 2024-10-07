package com.teamapi.palette.dto.response.chat

import com.teamapi.palette.entity.consts.ChatState
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class ChatResponse(
    val id: String?,
    val message: String?,
    val resource: ChatState,
    val datetime: Instant,
    val roomId: Long,
    val userId: Long,
    val isAi: Boolean,
    val promptId: String?,
)
