package com.teamapi.palette.repository.room

import com.teamapi.palette.entity.Room
import com.teamapi.palette.repository.mongo.MongoRepository
import org.bson.types.ObjectId

interface RoomRepository : MongoRepository<Room> {
    suspend fun findAllByUserId(userId: ObjectId): List<Room>
}
