package com.teamapi.palette.entity.chat

import com.teamapi.palette.dto.response.chat.ChatResponse
import com.teamapi.palette.entity.consts.ChatState
import com.teamapi.palette.repository.mongo.MongoDatabases
import com.teamapi.palette.util.InstantSerializer
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
@SerialName(MongoDatabases.CHAT)
data class Chat(
    @Contextual
    @SerialName("_id")
    val id: ObjectId = ObjectId.get(),
    @Serializable(with = InstantSerializer::class)
    val datetime: Instant = Clock.System.now(),
    val resource: ChatState = ChatState.CHAT,

    // default property
    val message: String? = null,
    @Contextual
    val roomId: ObjectId,
    @Contextual
    val userId: ObjectId,
    val isAi: Boolean,
    val regenScope: Boolean? = false,
    @Contextual
    val promptId: ObjectId? = null
) {
    fun toDto() = ChatResponse(
        id.toString(),
        message,
        resource,
        datetime,
        roomId.toString(),
        userId.toString(),
        isAi,
        regenScope ?: false,
        promptId?.toString()
    )
}
