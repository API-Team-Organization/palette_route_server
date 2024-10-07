package com.teamapi.palette.dto.response.chat

import kotlinx.serialization.Serializable

@Serializable
data class ImageListResponse(
    val images: List<String>
)
