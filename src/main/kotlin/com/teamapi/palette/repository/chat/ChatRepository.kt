package com.teamapi.palette.repository.chat

import com.teamapi.palette.entity.Chat
import org.springframework.data.domain.Pageable
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface ChatRepository : R2dbcRepository<Chat, Long> {
    fun findAllByRoomIdIsOrderByDatetimeDesc(roomId: Long, pageable: Pageable): Flux<Chat>
}
