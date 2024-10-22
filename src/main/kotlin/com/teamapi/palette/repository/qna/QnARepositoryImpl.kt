package com.teamapi.palette.repository.qna

import com.mongodb.client.model.*
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.teamapi.palette.entity.qna.QnA
import com.teamapi.palette.repository.mongo.MongoDatabases
import com.teamapi.palette.repository.mongo.eq
import kotlinx.coroutines.flow.firstOrNull
import org.springframework.stereotype.Repository

@Repository
class QnARepositoryImpl(
    private val mongo: MongoDatabase
) : QnARepository {

    override suspend fun create(prompt: QnA): QnA {
        val collection = mongo.getCollection<QnA>(MongoDatabases.QNA)

        return collection.findOneAndReplace(QnA::roomId eq prompt.roomId, prompt, FindOneAndReplaceOptions().returnDocument(ReturnDocument.AFTER).upsert(true))!!
    }

    override suspend fun getQnAByRoomId(roomId: Long): QnA? {
        val collection = mongo.getCollection<QnA>(MongoDatabases.QNA)

        return collection.find(QnA::roomId eq roomId)
            .firstOrNull()
    }

    override suspend fun deleteAllByRoomId(roomId: Long): Boolean {
        val collection = mongo.getCollection<QnA>(MongoDatabases.QNA)

        return collection.deleteMany(QnA::roomId eq roomId).wasAcknowledged()
    }
}
