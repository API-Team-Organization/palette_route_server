package com.teamapi.palette.dto.infra.req

import kotlinx.serialization.Serializable

@Serializable
data class DiscordMessage(
    val content: String? = null,
    val embeds: List<DiscordEmbed>? = null,
    val files: List<DiscordAttachments>? = null
)
