package com.teamapi.palette.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class PaletteProperties(
    @Value("\${palette.discord-webhook}")
    val discordWebhook: String
)
