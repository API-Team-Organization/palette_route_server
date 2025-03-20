package com.teamapi.palette.repository.chat

import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.FindOneAndReplaceOptions
import com.mongodb.client.model.Projections
import com.mongodb.client.model.ReturnDocument
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.teamapi.palette.dto.response.chat.ChatResponse
import com.teamapi.palette.entity.chat.Chat
import com.teamapi.palette.entity.consts.ChatState
import com.teamapi.palette.repository.mongo.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.util.Date

@Repository
class ChatRepositoryImpl(
    mongo: MongoDatabase
) : ChatRepository {
    // used internally
    private val collection = mongo.getCollection<Chat>(MongoDatabases.CHAT)
    internal data class ImageFoundResult(val message: String)
    internal data class LastMessageResult(@BsonId val id: ObjectId, val lastMessage: String?)

    override suspend fun getImagesByUserId(userId: ObjectId, pageable: Pageable): List<String> {

        return collection
            .find<ImageFoundResult>((Chat::userId eq userId) and (Chat::resource eq ChatState.IMAGE))
            .sort(Chat::datetime.desc())
            .skip(pageable.offset.toInt())
            .limit(pageable.pageSize)
            .projection(Projections.include(Chat::message.name))
            .map { it.message }
            .toList()
    }

    override suspend fun getMessageByRoomId(roomId: ObjectId, offset: Instant, size: Long): List<ChatResponse> {
        return collection
            .find(Chat::datetime lt Date.from(offset.toJavaInstant()) and (Chat::roomId eq roomId))
            .sort(Chat::datetime.desc())
            .limit(size.toInt())
            .toList()
            .map { it.toDto() }
    }

    override suspend fun getLatestChatByRoomId(roomId: ObjectId): Chat? {
        return collection
            .find(Chat::roomId eq roomId)
            .sort(Chat::datetime.desc())
            .limit(1)
            .firstOrNull()
    }

    override suspend fun getLatestMessageMapById(roomIds: List<ObjectId>): Map<ObjectId, String?> {
        return collection.aggregate<LastMessageResult>(
            Aggregates.match((Chat::roomId `in` roomIds) and (Chat::resource ne ChatState.IMAGE)),
            Aggregates.group(Chat::roomId.literal, Chat::message.getLastAs("lastMessage")),
        ).toList().associate { it.id to it.lastMessage }
    }

    override suspend fun create(chat: Chat): Chat {
        return collection.findOneAndReplace(
            Chat::id eq chat.id,
            chat,
            FindOneAndReplaceOptions().returnDocument(ReturnDocument.AFTER).upsert(true)
        )!!
    }

    override suspend fun deleteAllByRoomId(roomId: ObjectId): Boolean {
        return collection.deleteMany(Chat::roomId eq roomId).wasAcknowledged()
    }
}
