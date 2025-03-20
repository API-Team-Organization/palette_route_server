package com.teamapi.palette.service.adapter.image

import com.teamapi.palette.config.properties.PaletteProperties
import com.teamapi.palette.service.adapter.GenerativeImageAdapter
import com.teamapi.palette.service.adapter.comfy.GenerateRequest
import com.teamapi.palette.service.adapter.comfy.QueueResponse
import com.teamapi.palette.service.adapter.comfy.ws.ComfyWSBaseMessage
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitExchange
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import reactor.netty.http.client.WebsocketClientSpec
import java.net.URI
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.random.Random

class ComfyImageAdapter(
    private val client: WebClient,
    private val mapper: Json,
    private val palette: PaletteProperties
) : GenerativeImageAdapter {
    override suspend fun draw(prompt: GenerateRequest): Flow<ComfyWSBaseMessage> {
        return callbackFlow {
            val body = client.post()

                .uri("https://${palette.comfyUrl}/gen/flux")
                .bodyValue(mapper.encodeToString(prompt))
                .header("Authorization", "Basic ${generateAuthorization()}")
                .header("content-type", "application/json")
                .awaitExchange { it.awaitBody<QueueResponse>() }

            ReactorNettyWebSocketClient(HttpClient.create()) {
                WebsocketClientSpec.builder()
                    .maxFramePayloadLength(10 * 1024 * 1024)
            }.execute(URI.create("wss://${palette.comfyUrl}/ws?prompt=${body.promptId}"), HttpHeaders().apply {
                setBasicAuth(generateAuthorization())
            }) {
                mono {
                    val keepAlive = async {
                        while (isActive && it.isOpen) {
                            delay(10000L)
                            it.send(Mono.just(it.textMessage(Random.nextInt().toString()))).awaitSingleOrNull()
                        }
                    }
                    it
                        .receive()
                        .doOnNext { it.retain() } // keep the message. so we can use payloadAsText
                        .asFlow()
                        .cancellable()
                        .catch {
                            it.printStackTrace()
                            println("err 🫨")
                        }
                        .onCompletion {
                            it?.printStackTrace()
                            this@callbackFlow.close()
                        }
                        .filter { it.type == WebSocketMessage.Type.TEXT }
                        .collect {
                            try {
                                send(mapper.decodeFromString<ComfyWSBaseMessage>(it.payloadAsText))
                            } catch (e: Exception) {
                                e.printStackTrace()
                                // message death
                            } finally {
                                try {
                                    it.release()
                                } catch (ignored: Throwable) {
                                }
                            }
                        }
                    try {
                        keepAlive.cancelAndJoin()
                    } catch (ignored: Throwable) {
                    }
                }.then()
            }.awaitSingle()
            awaitClose()
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun generateAuthorization() =
        Base64.encode("${palette.comfyCredentials}:${palette.comfyPassword}".toByteArray())
}
