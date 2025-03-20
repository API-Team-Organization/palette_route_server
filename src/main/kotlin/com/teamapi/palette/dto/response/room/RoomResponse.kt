package com.teamapi.palette.dto.response.room

import kotlinx.serialization.Serializable

@Serializable
data class RoomResponse(
    val id: String,
    val title: String?,
    val message: String?
)
