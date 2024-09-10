package com.teamapi.palette.repository

import com.teamapi.palette.entity.Room
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface RoomRepository : R2dbcRepository<Room, Long> {
    fun findByUserId(userId: Long): Flux<Room>
}
