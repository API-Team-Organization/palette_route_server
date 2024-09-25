package com.teamapi.palette.dto.chat

import com.teamapi.palette.entity.chat.PromptData
import com.teamapi.palette.entity.consts.ChatState
import java.time.ZonedDateTime

data class ChatResponse(
    val id: String?,
    val message: String?,
    val resource: ChatState,
    val datetime: ZonedDateTime,
    val roomId: Long,
    val userId: Long,
    val isAi: Boolean,
    val data: PromptData?,
)
