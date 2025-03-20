package com.teamapi.palette.repository.qna

import com.teamapi.palette.entity.qna.QnA
import com.teamapi.palette.repository.mongo.MongoRepository
import org.bson.types.ObjectId

interface QnARepository : MongoRepository<QnA> {
    suspend fun getQnAByRoomId(roomId: ObjectId): QnA?
    suspend fun deleteAllByRoomId(roomId: ObjectId): Boolean
}
