package com.teamapi.palette.dto.response.chat

import com.teamapi.palette.entity.consts.ChatState
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
data class ChatResponse(
    val id: String,
    val message: String?,
    val resource: ChatState,
    val datetime: Instant,
    val roomId: String,
    val userId: String,
    val isAi: Boolean,
    val regenScope: Boolean,
    val promptId: String?,
)
