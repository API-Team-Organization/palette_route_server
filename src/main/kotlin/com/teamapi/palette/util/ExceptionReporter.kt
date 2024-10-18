package com.teamapi.palette.util

import com.teamapi.palette.config.PaletteProperties
import com.teamapi.palette.dto.infra.req.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitExchange

@Component
class ExceptionReporter(
    private val paletteProperties: PaletteProperties,
    private val webClient: WebClient,
    private val mapper: Json
) {
    fun doReport(e: Exception) {
        CoroutineScope(Dispatchers.Unconfined).async {
            reportException(e)
        }.invokeOnCompletion {
            it?.printStackTrace()
        }
    }

    suspend fun reportException(e: Exception) {
        val msg = DiscordMessage(
            content = "Exception Received",
            embeds = listOf(
                DiscordEmbed(
                    title = "Route Server got Exception!",
                    fields = listOf(
                        DiscordEmbedField(
                            "Root Exception Message",
                            e.message ?: "no description provided"
                        )
                    ) + if (e.cause != null) listOf(
                        DiscordEmbedField(
                            "Caused By",
                            e.cause!!.message ?: "no description provided"
                        )
                    ) else emptyList(),
                    footer = DiscordEmbedFooter("Full info in attached file.")
                )
            ),
            files = listOf(
                DiscordAttachments(
                    0,
                    "full_stacktrace.txt",
                    "Root Exception Stacktrace"
                )
            ) + if (e.cause != null) listOf(
                DiscordAttachments(
                    1,
                    "caused_by_stacktrace.txt",
                    "Caused Stacktrace"
                )
            ) else emptyList()
        )

        val multipartBodyBuilder = MultipartBodyBuilder()

        multipartBodyBuilder.part("payload_json", mapper.encodeToString(msg))
            .contentType(MediaType.APPLICATION_JSON)

        multipartBodyBuilder.part("file[0]", e.stackTraceToString())
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .filename("full_stacktrace.txt") // Optional: specify filename
        if (e.cause != null) {
            multipartBodyBuilder.part("file[1]", e.cause!!.stackTraceToString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .filename("caused_by_stacktrace.txt") // Optional: specify filename
        }

        webClient
            .post()
            .uri(paletteProperties.discordWebhook)
            .header("User-Agent", "DiscordBot (https://paletteapp.xyz, 1.0.0)")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
            .awaitExchange {
                it.releaseBody().awaitSingle()
            }
    }
}
