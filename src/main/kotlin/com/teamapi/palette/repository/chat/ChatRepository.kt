package com.teamapi.palette.repository.chat

import com.teamapi.palette.entity.Chat
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ChatRepository : CoroutineCrudRepository<Chat, Long> {
    suspend fun findAllByRoomIdIsOrderByDatetimeDesc(roomId: Long, pageable: Pageable): List<Chat>
}
