package com.teamapi.palette.ws.dto.res

import com.teamapi.palette.dto.response.chat.ChatResponse
import com.teamapi.palette.entity.consts.ChatState
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class NewChatMessage(
    val id: String?,
    val message: String?,
    val resource: ChatState,
    val datetime: Instant,
    val roomId: String,
    val userId: String,
    val isAi: Boolean,
    val regenScope: Boolean,
    val promptId: String?,
) : BaseResponseMessage {
    override val type = MessageType.NEW_CHAT

    companion object {
        fun fromDto(chat: ChatResponse) = NewChatMessage(
            chat.id,
            chat.message,
            chat.resource,
            chat.datetime,
            chat.roomId,
            chat.userId,
            chat.isAi,
            chat.regenScope,
            chat.promptId,
        )
    }
}
