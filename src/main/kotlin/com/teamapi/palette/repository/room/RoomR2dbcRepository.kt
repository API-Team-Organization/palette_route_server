package com.teamapi.palette.repository.room

import com.teamapi.palette.entity.Room
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface RoomR2dbcRepository : CoroutineCrudRepository<Room, Long> {
    fun findRoomByUserId(userId: Long): Flow<Room>
}
