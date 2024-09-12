package com.teamapi.palette.ws.actor

import com.teamapi.palette.entity.Chat
import com.teamapi.palette.ws.dto.RoomAction
import com.teamapi.palette.ws.dto.WSRoomMessage
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.reactive.asFlow
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.stereotype.Component
import reactor.core.publisher.Sinks.Many

@Component
class SinkActor(
    private val sink: Many<WSRoomMessage>
) : DisposableBean {
    private val log = LoggerFactory.getLogger(SinkActor::class.java)

    private val roomActors: MutableSet<SendChannel<RoomMessages>> = hashSetOf()
    private val sinkWorker = CoroutineScope(Dispatchers.Unconfined).async {
        log.info("Sink Listener Working!")
        try {
            sink
                .asFlux()
                .asFlow()
                .cancellable()
                .onCompletion {
                    println("WTH?!?!?!?")
                }
                .catch {
                    it.printStackTrace()
                }
                .onEmpty {
                    println("AAA")
                }
                .collect {
                    log.info("{}: [{}] {}", it.roomId, it.action, it.message)
                    for (actor in roomActors) {
                        actor.send(RoomMessages.NewChat(it.roomId, it.action, it.message))
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        log.info("Sink Listener dies...")
    }

    @OptIn(DelicateCoroutinesApi::class, ObsoleteCoroutinesApi::class)
    private val actorInstance = CoroutineScope(Dispatchers.Unconfined).actor<SinkMessages> {
        for (msg in channel) {
            synchronized(roomActors) {
                when (msg) {
                    is SinkMessages.AddChat -> {
                        val emit = sink.tryEmitNext(msg.toActorMessage())
                        emit.orThrow()
                    }

                    is SinkMessages.Listen -> {
                        roomActors.add(msg.actor)
                    }

                    is SinkMessages.Dispose -> {
                        roomActors.remove(msg.actor)
                    }

                    SinkMessages.CleanUp -> {
                        roomActors.removeIf { it.isClosedForSend }
                    }
                }
            }
        }
    }

    suspend fun send(msg: SinkMessages) {
        actorInstance.send(msg)
    }

    suspend fun addChat(roomId: Long, action: RoomAction, message: Chat?) {
        send(SinkMessages.AddChat(roomId, action, message))
    }

    override fun destroy() {
        roomActors.clear()
        runBlocking {
            sinkWorker.cancelAndJoin()
        }

        log.info("SinkActor disposed! (destroyed: {}, active: {})", sinkWorker.isCompleted, sinkWorker.isActive)
    }
}

sealed interface SinkMessages {
    data class Listen(val actor: SendChannel<RoomMessages>) : SinkMessages
    data class Dispose(val actor: SendChannel<RoomMessages>) : SinkMessages
    data object CleanUp : SinkMessages
    data class AddChat(val roomId: Long, val action: RoomAction, val message: Chat?) : SinkMessages {
        fun toActorMessage() = WSRoomMessage(roomId, action, message)
    }
}
