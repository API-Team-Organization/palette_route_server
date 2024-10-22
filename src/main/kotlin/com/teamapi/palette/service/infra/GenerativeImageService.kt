package com.teamapi.palette.service.infra

import com.teamapi.palette.service.infra.comfy.GenerateRequest
import com.teamapi.palette.service.infra.comfy.QueueResponse
import com.teamapi.palette.service.infra.comfy.ws.ComfyWSBaseMessage
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
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitExchange
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import reactor.netty.http.client.WebsocketClientSpec
import java.net.URI
import kotlin.random.Random

@Service
class GenerativeImageService(
    private val client: WebClient,
    private val mapper: Json,
) {
    suspend fun draw(prompt: GenerateRequest): Flow<ComfyWSBaseMessage> {
        val body = client.post()
            .uri("https://comfy.paletteapp.xyz/gen/flux")
            .bodyValue(mapper.encodeToString(prompt))
            .header("content-type", "application/json")
            .awaitExchange { it.awaitBody<QueueResponse>() }

        println(body)
        return callbackFlow {
            ReactorNettyWebSocketClient(HttpClient.create()) {
                WebsocketClientSpec.builder()
                    .maxFramePayloadLength(10 * 1024 * 1024)
            }.execute(URI.create("wss://comfy.paletteapp.xyz/ws?prompt=${body.promptId}")) {
                mono {
                    val keepAlive = async {
                        while (isActive && it.isOpen) {
                            delay(10000L)
                            it.send(Mono.just(it.textMessage(Random.nextInt().toString()))).awaitSingleOrNull()
                        }
                    }
                    it.receive()
                        .doOnNext { it.retain() } // keep the message. so we can use payloadAsText
                        .asFlow()
                        .cancellable()
                        .catch {
                            it.printStackTrace()
                            println("err 🫨")
                        }
                        .onCompletion {
                            it?.printStackTrace()
                            println("close")
                            this@callbackFlow.close()
                        }
                        .filter { it.type == WebSocketMessage.Type.TEXT }
                        .collect {
                            try {
                                send(mapper.decodeFromString<ComfyWSBaseMessage>(it.payloadAsText))
                                it.release()
                            } catch (e: Exception) {
                                e.printStackTrace()
                                // message death
                            }
                        }
                    try {
                        keepAlive.cancelAndJoin()
                    } catch (e: Throwable) {
                        // ignored
                    }
                }.then()
            }.awaitSingle()
            awaitClose()
        }
    }

}
