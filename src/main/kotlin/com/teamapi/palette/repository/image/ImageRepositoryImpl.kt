package com.teamapi.palette.repository.image

import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.teamapi.palette.entity.image.Image
import com.teamapi.palette.repository.mongo.eq
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId

class ImageRepositoryImpl(database: MongoDatabase) : ImageRepository {
    private val template = database.getCollection<Image>("image")

    override suspend fun findAll(): List<Image> {
        return template.find().toList()
    }

    override suspend fun <ITEM : Image> create(item: ITEM): ITEM {
        val id = template
            .insertOne(item)
            .insertedId?.asObjectId()?.value
            ?: error("Cannot create object")

        @Suppress("UNCHECKED_CAST")
        return findByIdOrNull(id) as ITEM
    }

    override suspend fun findByIdOrNull(id: ObjectId): Image? {
        return template
            .find(Image::id eq id)
            .firstOrNull()
    }

    override suspend fun <ITEM : Image> modify(item: ITEM): Boolean {
        return template
            .replaceOne(Image::id eq item.id, item)
            .modifiedCount == 1L
    }

    override suspend fun deleteById(id: ObjectId): Boolean {
        return template
            .deleteOne(Image::id eq id)
            .deletedCount == 1L
    }
}
