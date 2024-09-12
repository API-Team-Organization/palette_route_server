package com.teamapi.palette.repository.chat

import com.teamapi.palette.entity.Chat
import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ChatR2dbcRepository : CoroutineCrudRepository<Chat, Long> {
    @Suppress("SpringDataMethodInconsistencyInspection")
    suspend fun findChatByIsAiIsAndUserIdIsAndResourceIs(isAi: Boolean, userId: Long, resource: String, pageable: Pageable): Flow<Chat>
}
