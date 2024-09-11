package com.teamapi.palette.ws.actor

import com.teamapi.palette.dto.chat.ChatResponse
import com.teamapi.palette.repository.RoomRepository
import com.teamapi.palette.response.ErrorCode
import com.teamapi.palette.ws.dto.WSRoomMessage
import com.teamapi.palette.ws.dto.res.ChatMessage
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.reactive.asFlow
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import reactor.core.publisher.Sinks
import reactor.core.publisher.Sinks.Many

@Component
class WSRoomActor(
    private val roomRepository: RoomRepository,
    @Lazy private val sink: Many<WSRoomMessage>
) {
    private val log = LoggerFactory.getLogger(WSRoomActor::class.java)

    fun emit(msg: WSRoomMessage) {
        sink.emitNext(msg, Sinks.EmitFailureHandler.FAIL_FAST)
    }

    @OptIn(DelicateCoroutinesApi::class)
    @ObsoleteCoroutinesApi
    operator fun invoke(roomId: Long, delegateActor: SendChannel<DelegateMessage>) =
        CoroutineScope(Dispatchers.Unconfined).actor<RoomMessage> {
            val roomHooked = roomRepository.findById(roomId)
                ?: return@actor delegateActor.trySend(DelegateMessage.DisconnectWithError(ErrorCode.ROOM_NOT_FOUND))
                    .let {}

            delegateActor.trySend(DelegateMessage.Validate(roomHooked.userId))

            sink
                .asFlux()
                .filter { it.roomId == roomHooked.id }
                .asFlow()
                .cancellable()
                .collect {
                    if (delegateActor.isClosedForSend)
                        return@collect coroutineContext.cancel()

                    log.info("{}: [{}] {}", it.roomId, it.action, it.message)
                    delegateActor.send(
                        DelegateMessage.SendMessage(
                            ChatMessage(
                                it.action,
                                it.message?.let { msg ->
                                    ChatResponse(
                                        msg.id,
                                        msg.message,
                                        msg.datetime,
                                        msg.roomId,
                                        msg.userId,
                                        msg.isAi,
                                        msg.resource
                                    )
                                }
                            )
                        )
                    )
                }
        }
}

sealed interface RoomMessage {
//    data class
}
