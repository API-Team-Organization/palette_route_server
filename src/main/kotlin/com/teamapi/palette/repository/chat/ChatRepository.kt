package com.teamapi.palette.repository.chat

import com.teamapi.palette.dto.response.chat.ChatResponse
import com.teamapi.palette.entity.chat.Chat
import kotlinx.datetime.Instant
import org.springframework.data.domain.Pageable

interface ChatRepository {
    suspend fun getImagesByUserId(userId: Long, pageable: Pageable): List<String>
    suspend fun getMessageByRoomId(roomId: Long, offset: Instant, size: Long): List<ChatResponse>
    suspend fun getLatestMessageMapById(roomIds: List<Long>): Map<Long, String?>
    suspend fun getLatestChatByRoomId(roomId: Long): Chat?
    suspend fun create(chat: Chat): Chat
}
