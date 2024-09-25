package com.teamapi.palette.entity.chat

import com.teamapi.palette.dto.chat.ChatResponse
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.relational.core.mapping.Column
import java.time.LocalDateTime

@Document("chats")
data class Chat(
    @Id
    val id: Long? = null,
    val message: String,
    val resource: String = "CHAT",
    val datetime: LocalDateTime,
    @Column("room_id")
    val roomId: Long,
    @Column("user_id")
    val userId: Long,
    @Column("is_ai")
    val isAi: Boolean
) {
    fun toDto() = ChatResponse(id, message, datetime, roomId, userId, isAi, resource)
}
