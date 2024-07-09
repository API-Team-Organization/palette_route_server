package com.teamapi.palette.repository

import com.teamapi.palette.entity.Room
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface RoomRepository : R2dbcRepository<Room, Long> {
    fun findByUserId(userId: Long): Flux<Room>
}