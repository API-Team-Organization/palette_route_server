package com.teamapi.palette.repository.chat

import com.teamapi.palette.dto.chat.ChatResponse
import org.springframework.data.domain.Pageable
import java.time.ZonedDateTime

interface ChatQueryRepository {
    suspend fun getImagesByUserId(userId: Long, pageable: Pageable): List<String>
    suspend fun getMessageByRoomId(roomId: Long, offset: ZonedDateTime, size: Long): List<ChatResponse>
    suspend fun getLatestMessageMapById(roomIds: List<Long>): Map<Long, String?>
}
