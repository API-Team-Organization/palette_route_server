package com.teamapi.palette.config

import org.springframework.ai.anthropic.AnthropicChatModel
import org.springframework.ai.anthropic.AnthropicChatOptions
import org.springframework.ai.anthropic.api.AnthropicApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ChatModelConfig {
    @Bean
    fun anthropicChatModel(anthropicApi: AnthropicApi): AnthropicChatModel = AnthropicChatModel.builder().apply {
        anthropicApi(anthropicApi)
        defaultOptions(
            AnthropicChatOptions.builder().apply {
                temperature(0.5)
                maxTokens(200)
                model("claude-3-7-sonnet-20250219")
            }.build()
        )
    }.build()
}
