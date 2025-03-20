package com.teamapi.palette.entity

import com.teamapi.palette.entity.consts.UserState
import com.teamapi.palette.repository.mongo.MongoDatabases
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId
import kotlinx.datetime.LocalDate

@Serializable
@SerialName(MongoDatabases.USER)
data class User(
    val email: String,
    val password: String, // hashed
    val username: String,

    val birthDate: LocalDate,

    val state: UserState = UserState.CREATED,

    @SerialName("_id")
    @Contextual
    val id: ObjectId = ObjectId.get(),
)
