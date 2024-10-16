package com.teamapi.palette.service.infra

import com.teamapi.palette.service.infra.comfy.GenerateRequest
import com.teamapi.palette.service.infra.comfy.GenerateResponse
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitExchange

@Service
class GenerativeImageService(
    private val client: WebClient,
    private val mapper: Json,
) {
    suspend fun draw(prompt: GenerateRequest): GenerateResponse {
        return client.post()
            .uri("https://comfy.paletteapp.xyz/gen")
            .bodyValue(mapper.encodeToString(prompt))
            .header("content-type", "application/json")
            .awaitExchange { it.awaitBody<GenerateResponse>() }
    }

}
