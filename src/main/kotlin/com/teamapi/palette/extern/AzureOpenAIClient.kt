package com.teamapi.palette.extern

import com.azure.ai.openai.OpenAIAsyncClient
import com.azure.ai.openai.OpenAIClientBuilder
import com.azure.core.credential.AzureKeyCredential
import com.teamapi.palette.config.OpenAIProperties
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@Component
class AzureOpenAIClient(private val properties: OpenAIProperties) {
    @Bean
    fun azure(): OpenAIAsyncClient =
        OpenAIClientBuilder()
            .endpoint(properties.endpoint)
            .credential(AzureKeyCredential(properties.apiKey))
            .buildAsyncClient()
}
