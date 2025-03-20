package com.teamapi.palette.repository.chat

import com.teamapi.palette.dto.response.chat.ChatResponse
import com.teamapi.palette.entity.chat.Chat
import kotlinx.datetime.Instant
import org.bson.types.ObjectId
import org.springframework.data.domain.Pageable

interface ChatRepository {
    suspend fun getImagesByUserId(userId: ObjectId, pageable: Pageable): List<String>
    suspend fun getMessageByRoomId(roomId: ObjectId, offset: Instant, size: Long): List<ChatResponse>
    suspend fun getLatestMessageMapById(roomIds: List<ObjectId>): Map<ObjectId, String?>
    suspend fun create(chat: Chat): Chat

    suspend fun getLatestChatByRoomId(roomId: ObjectId): Chat?
    suspend fun deleteAllByRoomId(roomId: ObjectId): Boolean
}
