package com.teamapi.palette.entity.chat

import com.teamapi.palette.dto.chat.ChatResponse
import com.teamapi.palette.entity.consts.ChatState
import com.teamapi.palette.repository.mongo.MongoDatabases
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.mapping.Document
import java.time.ZonedDateTime

@Document(MongoDatabases.CHAT)
data class Chat(
    @BsonId
    val id: ObjectId = ObjectId.get(),
    val datetime: ZonedDateTime, // MUST-INCLUDED
    val resource: ChatState = ChatState.CHAT,

    // default property
    val message: String? = null,
    val roomId: Long,
    val userId: Long,
    val isAi: Boolean,

    // additional prompt data
    val data: PromptData? = null
) {
    fun toDto() = ChatResponse(id.toString(), message, resource, datetime, roomId, userId, isAi, data)
}
