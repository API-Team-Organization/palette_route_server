package com.teamapi.palette.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class OpenAIProperties(
    @Value("\${spring.ai.azure.openai.api-key}")
    val apiKey: String,

    @Value("\${spring.ai.azure.openai.endpoint}")
    val endpoint: String
)
