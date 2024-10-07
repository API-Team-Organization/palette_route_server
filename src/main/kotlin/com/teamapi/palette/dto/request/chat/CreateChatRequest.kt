package com.teamapi.palette.dto.request.chat

import com.teamapi.palette.entity.qna.ChatAnswer
import kotlinx.serialization.Serializable

@Serializable
data class CreateChatRequest(
    @Serializable(with = ChatAnswer.ChatAnswerSerializer::class)
    val data: ChatAnswer,
)
