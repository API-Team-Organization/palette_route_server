package com.teamapi.palette.dto.response

import com.teamapi.palette.entity.consts.PromptType
import com.teamapi.palette.entity.qna.ChatAnswer
import com.teamapi.palette.entity.qna.ChatQuestion
import kotlinx.serialization.Serializable

@Serializable
sealed interface RoomResponses : Responses {
    @Serializable
    data class RoomResponse(
        val id: Long,
        val title: String?,
        val message: String?
    ) : RoomResponses

    @Serializable
    data class QnAResponse(
        val id: String,
        val type: PromptType,
        val question: ChatQuestion,
        val answer: ChatAnswer?,
        val promptName: String,
    ) : RoomResponses
}
