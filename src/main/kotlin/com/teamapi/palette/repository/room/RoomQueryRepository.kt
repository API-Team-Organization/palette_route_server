package com.teamapi.palette.repository.room

import com.teamapi.palette.dto.room.RoomResponse

interface RoomQueryRepository {
    suspend fun findRoomByUserId(userId: Long): List<RoomResponse>
}
