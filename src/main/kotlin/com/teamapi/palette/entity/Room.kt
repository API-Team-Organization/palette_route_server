package com.teamapi.palette.entity

import com.teamapi.palette.repository.mongo.MongoDatabases
import com.teamapi.palette.response.ErrorCode
import com.teamapi.palette.response.exception.CustomException
import com.teamapi.palette.service.SessionHolder
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
@SerialName(MongoDatabases.ROOM)
data class Room(
    @SerialName("_id")
    @Contextual
    val id: ObjectId = ObjectId.get(),
    @Contextual
    val userId: ObjectId,
    val title: String? = "New Chat",
) {
    suspend fun validateUser(sessionHolder: SessionHolder): Room {
        val me = sessionHolder.me()
        if (userId != me)
            throw CustomException(ErrorCode.NOT_YOUR_ROOM)
        return this
    }
}
