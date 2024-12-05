package com.teamapi.palette.config.properties

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class PaletteProperties(
    @Value("\${palette.discord-webhook}")
    val discordWebhook: String,
    @Value("\${palette.comfy-url}")
    val comfyUrl: String,
    @Value("\${palette.comfy-credentials}")
    val comfyCredentials: String,
    @Value("\${palette.comfy-password}")
    val comfyPassword: String,
)
