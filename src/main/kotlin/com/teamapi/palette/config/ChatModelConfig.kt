package com.teamapi.palette.config

import com.azure.ai.openai.OpenAIClientBuilder
import com.teamapi.palette.config.properties.AnthropicProperties
import org.springframework.ai.anthropic.AnthropicChatModel
import org.springframework.ai.anthropic.AnthropicChatOptions
import org.springframework.ai.anthropic.api.AnthropicApi
import org.springframework.ai.azure.openai.AzureOpenAiChatModel
import org.springframework.ai.azure.openai.AzureOpenAiChatOptions
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.NoOpResponseErrorHandler
import org.springframework.web.client.RestClient
import org.springframework.web.reactive.function.client.WebClient


@Configuration
class ChatModelConfig(
    private val anthropic: AnthropicProperties,
) {
    @Bean
    fun anthropicApi(): AnthropicApi = AnthropicApi(
        AnthropicApi.DEFAULT_BASE_URL,
        anthropic.apiKey,
        AnthropicApi.DEFAULT_ANTHROPIC_VERSION,
        RestClient.builder(),
        WebClient.builder()
            .defaultStatusHandler({ it.isError }, {
                it.bodyToMono(String::class.java).map { body ->
                    RuntimeException("Response exception, Status: [${it.statusCode()}], Body:[$body]")
                }
            }),
        NoOpResponseErrorHandler(),
    )

    @Bean
    fun anthropicChatModel(anthropicApi: AnthropicApi) = AnthropicChatModel(
        anthropicApi,
        AnthropicChatOptions().apply {
            temperature = 0.5
            maxTokens = 200
            model = "claude-3-5-sonnet-20240620"
        }
    )

    @Bean
    fun openAiChatModel(openAiClient: OpenAIClientBuilder) = AzureOpenAiChatModel(
        openAiClient,
        AzureOpenAiChatOptions().apply {
            temperature = 0.7
            maxTokens = 200
            deploymentName = "PaletteGPT"
        }
    )
}
