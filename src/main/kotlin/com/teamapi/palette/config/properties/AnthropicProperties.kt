package com.teamapi.palette.config.properties

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class AnthropicProperties(
    @Value("\${spring.ai.anthropic.api-key}")
    val apiKey: String
)
