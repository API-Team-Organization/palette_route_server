package com.teamapi.palette.repository.chat

import com.teamapi.palette.dto.chat.ChatResponse
import java.time.LocalDateTime

interface ChatQueryRepository {
    // suspend fun findAllByRoomIdIsOrderByDatetimeDesc(roomId: Long, pageable: Pageable): Flow<Chat>
    suspend fun findPagedWithLastMessageId(roomId: Long, before: LocalDateTime, size: Long): List<ChatResponse>
}
