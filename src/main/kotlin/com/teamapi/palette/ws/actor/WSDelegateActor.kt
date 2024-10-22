package com.teamapi.palette.ws.actor

import com.teamapi.palette.response.ErrorCode
import com.teamapi.palette.ws.dto.res.BaseResponseMessage
import com.teamapi.palette.ws.ext.sendAndClose
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.kotlin.core.publisher.toMono

@Component
class WSDelegateActor(
    private val mapper: Json,
    private val sinkActor: SinkActor
) {
    @ObsoleteCoroutinesApi
    operator fun invoke(session: WebSocketSession, principal: UserDetails): SendChannel<DelegateMessage>
    = CoroutineScope(Dispatchers.Unconfined).actor {
        this.channel.invokeOnClose {
            runBlocking {
                session.handleMessage(DelegateMessage.Close, principal) // auto close
            }
        }

        for (msg in channel) {
            session.handleMessage(msg, principal)
        }
    }

    private suspend fun WebSocketSession.handleMessage(msg: DelegateMessage, principal: UserDetails) {
        when (msg) {
            is DelegateMessage.DisconnectWithError -> sendAndClose(mapper, msg.error)
            is DelegateMessage.SendMessage -> send(textMessage(mapper.encodeToString(msg.data)).toMono()).awaitSingleOrNull()
            is DelegateMessage.Validate -> {
                if (principal.username?.toLongOrNull() != msg.id) {
                    handleMessage(DelegateMessage.DisconnectWithError(ErrorCode.NOT_YOUR_ROOM), principal)
                    return
                }
                msg.onSuccess(sinkActor)
            }
            DelegateMessage.Close -> close().awaitSingleOrNull()
        }
    }
}

sealed interface DelegateMessage {
    data class DisconnectWithError(val error: ErrorCode) : DelegateMessage
    data class SendMessage(val data: BaseResponseMessage): DelegateMessage
    data object Close : DelegateMessage
    data class Validate(val id: Long, val onSuccess: suspend (SinkActor) -> Unit) : DelegateMessage
}
