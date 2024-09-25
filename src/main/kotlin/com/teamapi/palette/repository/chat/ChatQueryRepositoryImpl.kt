package com.teamapi.palette.repository.chat

import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Projections
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.teamapi.palette.dto.chat.ChatResponse
import com.teamapi.palette.entity.chat.Chat
import com.teamapi.palette.entity.consts.ChatState
import com.teamapi.palette.repository.mongo.*
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.bson.codecs.pojo.annotations.BsonId
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime
import java.time.ZonedDateTime

class ChatQueryRepositoryImpl(
    private val mongoDb: MongoDatabase
) : ChatQueryRepository {
    // used internally
    internal data class ImageFoundResult(val message: String)
    internal data class LastMessageResult(@BsonId val id: Long, val lastMessage: String?)

    override suspend fun getImagesByUserId(userId: Long, pageable: Pageable): List<String> {
        val collection = mongoDb.getCollection<Chat>(MongoDatabases.CHAT)

        return collection
            .find<ImageFoundResult>((Chat::userId eq userId) and (Chat::resource eq ChatState.IMAGE))
            .sort(Chat::datetime.desc())
            .skip(pageable.offset.toInt())
            .limit(pageable.pageSize)
            .projection(Projections.include(Chat::message.name))
            .map { it.message }
            .toList()
    }

    override suspend fun getMessageByRoomId(roomId: Long, offset: ZonedDateTime, size: Long): List<ChatResponse> {
        val collection = mongoDb.getCollection<Chat>(MongoDatabases.CHAT)

        return collection
            .find(Chat::datetime lt offset and (Chat::roomId eq roomId))
            .sort(Chat::datetime.desc())
            .limit(size.toInt())
            .toList()
            .map { it.toDto() }
    }

    override suspend fun getLatestMessageMapById(roomIds: List<Long>): Map<Long, String?> {
        val collection = mongoDb.getCollection<Chat>(MongoDatabases.CHAT)

        return collection.aggregate<LastMessageResult>(
            Aggregates.match((Chat::roomId `in` roomIds) and (Chat::resource ne ChatState.IMAGE)),
            Aggregates.group(Chat::roomId.literal, Chat::message.getLastAs("lastMessage")),
        ).toList().associate { it.id to it.lastMessage }
    }
}