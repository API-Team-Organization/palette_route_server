package com.teamapi.palette.config

import com.azure.ai.openai.OpenAIAsyncClient
import com.azure.ai.openai.OpenAIClientBuilder
import com.azure.core.credential.AzureKeyCredential
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenAIConfig(private val properties: OpenAIProperties) {
    @Bean
    fun azure(): OpenAIAsyncClient =
        OpenAIClientBuilder()
            .endpoint(properties.endpoint)
            .credential(AzureKeyCredential(properties.apiKey))
            .buildAsyncClient()
}
