package com.teamapi.palette.repository.qna

import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.teamapi.palette.entity.qna.QnA
import com.teamapi.palette.repository.mongo.MongoDatabases
import com.teamapi.palette.repository.mongo.eq
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId
import org.springframework.stereotype.Repository

@Repository
class QnARepositoryImpl(
    database: MongoDatabase
) : QnARepository {
    private val template = database.getCollection<QnA>(MongoDatabases.QNA)
    override suspend fun getQnAByRoomId(roomId: ObjectId): QnA? {
        return template
            .find(
                QnA::roomId eq roomId
            )
            .firstOrNull()
    }

    override suspend fun deleteAllByRoomId(roomId: ObjectId): Boolean {
        return template
            .deleteMany(
                QnA::roomId eq roomId
            )
            .deletedCount != 0L
    }

    override suspend fun findAll(): List<QnA> {
        return template
            .find()
            .toList()
    }

    override suspend fun <ITEM : QnA> create(item: ITEM): ITEM {
        val id = template
            .insertOne(item)
            .insertedId?.asObjectId()?.value
            ?: error("Cannot create object")

        @Suppress("UNCHECKED_CAST")
        return findByIdOrNull(id) as ITEM
    }

    override suspend fun findByIdOrNull(id: ObjectId): QnA? {
        return template
            .find(QnA::id eq id)
            .firstOrNull()
    }

    override suspend fun <ITEM : QnA> modify(item: ITEM): Boolean {
        return template
            .replaceOne(QnA::id eq item.id, item)
            .modifiedCount == 1L
    }

    override suspend fun deleteById(id: ObjectId): Boolean {
        return template
            .deleteOne(QnA::id eq id)
            .deletedCount == 1L
    }
}
