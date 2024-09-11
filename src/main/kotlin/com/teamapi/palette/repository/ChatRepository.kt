package com.teamapi.palette.repository

import com.teamapi.palette.entity.Chat
import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ChatRepository : CoroutineCrudRepository<Chat, Long> {
    suspend fun findAllByRoomIdIsOrderByDatetimeDesc(roomId: Long, pageable: Pageable): Flow<Chat>

    @Suppress("SpringDataMethodInconsistencyInspection")
    suspend fun findChatByIsAiIsAndUserIdIsAndResourceIs(isAi: Boolean, userId: Long, resource: String, pageable: Pageable): Flow<Chat>
}
