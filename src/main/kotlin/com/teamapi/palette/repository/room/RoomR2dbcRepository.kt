package com.teamapi.palette.repository.room

import com.teamapi.palette.entity.Room
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface RoomR2dbcRepository : CoroutineCrudRepository<Room, Long>
