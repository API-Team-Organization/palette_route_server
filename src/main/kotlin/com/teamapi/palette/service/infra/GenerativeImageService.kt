package com.teamapi.palette.service.infra

import com.azure.ai.openai.OpenAIAsyncClient
import com.azure.ai.openai.models.ImageGenerationOptions
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Service

@Service
class GenerativeImageService(
    private val azure: OpenAIAsyncClient,
    private val mapper: Json,
) {
    fun draw(originalText: String) =
        azure.getImageGenerations("Dalle3", ImageGenerationOptions(originalText))
            .handleAzureError(mapper)
}
