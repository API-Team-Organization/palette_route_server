package com.teamapi.palette.repository

import com.teamapi.palette.entity.Chat
import org.springframework.data.domain.Pageable
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Flux

interface ChatRepository : R2dbcRepository<Chat, Long> {
    fun findByRoomId(roomId: Long): Flux<Chat>
    fun findByIsAiAndUserId(isAi: Boolean, userId: Long, pageable: Pageable): Flux<Chat>
}