package com.teamapi.palette.dto.response.room

import com.teamapi.palette.entity.consts.PromptType
import com.teamapi.palette.entity.qna.ChatAnswer
import com.teamapi.palette.entity.qna.ChatQuestion
import kotlinx.serialization.Serializable

@Serializable
data class QnAResponse(
    val id: String,
    val type: PromptType,
    val question: ChatQuestion,
    val answer: ChatAnswer?,
    val promptName: String,
)
