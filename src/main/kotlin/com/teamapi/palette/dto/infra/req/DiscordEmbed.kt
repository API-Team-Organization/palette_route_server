package com.teamapi.palette.dto.infra.req

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DiscordEmbed(
    val title: String? = null,
    val type: String? = null,
    val description: String? = null,
    val url: String? = null,
    val timestamp: Instant? = null,
    val color: Int? = null,
    val footer: DiscordEmbedFooter? = null,
    val image: DiscordEmbedImage? = null,
    val thumbnail: DiscordEmbedImage? = null,
    val video: DiscordEmbedVideo? = null,
    val provider: DiscordEmbedProvider? = null,
    val author: DiscordEmbedAuthor? = null,
    val fields: List<DiscordEmbedField>? = null,
)

@Serializable
data class DiscordEmbedFooter(
    val text: String,
    @SerialName("icon_url")
    val iconUrl: String? = null,
    @SerialName("proxy_icon_url")
    val proxyIconUrl: String? = null
)

@Serializable
data class DiscordEmbedImage(
    val url: String,
    @SerialName("proxy_url")
    val proxyUrl: String? = null,
    val width: Int? = null,
    val height: Int? = null,
)

@Serializable
data class DiscordEmbedVideo(
    val url: String? = null,
    @SerialName("proxy_url")
    val proxyUrl: String? = null,
    val width: Int? = null,
    val height: Int? = null,
)

@Serializable
data class DiscordEmbedProvider(
    val name: String? = null,
    val url: String? = null,
)

@Serializable
data class DiscordEmbedAuthor(
    val name: String,
    val url: String? = null,
    @SerialName("icon_url")
    val iconUrl: String? = null,
    @SerialName("proxy_icon_url")
    val proxyIconUrl: String? = null
)

@Serializable
data class DiscordEmbedField(
    val name: String,
    val value: String,
    val inline: Boolean? = null,
)
