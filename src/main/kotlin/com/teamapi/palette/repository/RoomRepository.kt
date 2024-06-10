package com.teamapi.palette.repository

import com.teamapi.palette.entity.Room
import org.springframework.data.r2dbc.repository.R2dbcRepository

interface RoomRepository : R2dbcRepository<Room, Long> {
}