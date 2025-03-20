package com.teamapi.palette.entity.image

import com.teamapi.palette.repository.mongo.MongoDatabases
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
@SerialName(MongoDatabases.IMAGE)
class Image(
    val url: String,

    @Contextual
    @SerialName("_id")
    val id: ObjectId = ObjectId.get(),
)
