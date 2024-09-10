package com.teamapi.palette.repository

import com.teamapi.palette.entity.Room
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RoomRepository : CoroutineCrudRepository<Room, Long> {
    suspend fun findByUserId(userId: Long): List<Room>
}
