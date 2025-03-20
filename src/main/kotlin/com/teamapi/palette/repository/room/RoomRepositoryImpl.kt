package com.teamapi.palette.repository.room

import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.teamapi.palette.entity.Room
import com.teamapi.palette.repository.mongo.MongoDatabases
import com.teamapi.palette.repository.mongo.eq
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId
import org.springframework.stereotype.Repository

@Repository
class RoomRepositoryImpl(
    database: MongoDatabase
) : RoomRepository {
    private val template = database.getCollection<Room>(MongoDatabases.ROOM)

    override suspend fun findAllByUserId(userId: ObjectId): List<Room> {
        return template
            .find(Room::userId eq userId)
            .toList()
    }

    override suspend fun findAll(): List<Room> {
        return template
            .find()
            .toList()
    }

    override suspend fun <ITEM : Room> create(item: ITEM): ITEM {
        val id = template
            .insertOne(item)
            .insertedId?.asObjectId()?.value
            ?: error("Cannot create object")

        @Suppress("UNCHECKED_CAST")
        return findByIdOrNull(id) as ITEM
    }

    override suspend fun findByIdOrNull(id: ObjectId): Room? {
        return template
            .find(Room::id eq id)
            .firstOrNull()
    }

    override suspend fun <ITEM : Room> modify(item: ITEM): Boolean {
        return template
            .replaceOne(Room::id eq item.id, item)
            .modifiedCount == 1L
    }

    override suspend fun deleteById(id: ObjectId): Boolean {
        return template
            .deleteOne(Room::id eq id)
            .deletedCount == 1L
    }
}
