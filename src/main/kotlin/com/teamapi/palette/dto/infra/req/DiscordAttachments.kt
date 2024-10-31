package com.teamapi.palette.dto.infra.req

import kotlinx.serialization.Serializable

@Serializable
data class DiscordAttachments(
    val id: Int,
    val filename: String,
    val description: String? = null,
)
