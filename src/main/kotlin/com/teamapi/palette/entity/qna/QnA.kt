package com.teamapi.palette.entity.qna

import com.teamapi.palette.repository.mongo.MongoDatabases
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
@SerialName(MongoDatabases.QNA)
data class QnA(
    @SerialName("_id")
    @Contextual
    val id: ObjectId = ObjectId.get(),
    val roomId: Long,
    val qna: List<PromptData>
)
