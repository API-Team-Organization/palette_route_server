package com.teamapi.palette.ws.handler

import com.teamapi.palette.response.ErrorCode
import com.teamapi.palette.ws.actor.SinkActor
import com.teamapi.palette.ws.actor.SinkMessages
import com.teamapi.palette.ws.actor.WSRoomActor
import com.teamapi.palette.ws.actor.WSDelegateActor
import com.teamapi.palette.ws.ext.sendAndClose
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import java.net.URI

@Component
class CustomWebSocketHandler(
    private val mapper: Json,
    private val wsDelegateActor: WSDelegateActor,
    private val wsRoomActor: WSRoomActor,
    private val sinkActor: SinkActor
) : WebSocketHandler {
    private val log = LoggerFactory.getLogger(CustomWebSocketHandler::class.java)

    override fun handle(session: WebSocketSession): Mono<Void> {
        return mono {
            handleSuspend(session)
            log.info("Connection {} finish", session.id)
        }.then()
    }

    fun getRoomIdFromUrl(url: URI): String? {
        return url.path.let { it.substring(it.lastIndexOf('/') + 1) }
    }

    @OptIn(ObsoleteCoroutinesApi::class)
    suspend fun handleSuspend(session: WebSocketSession) {
        val principal = session.handshakeInfo.principal.awaitSingle() as Authentication

        val roomId = getRoomIdFromUrl(session.handshakeInfo.uri)
            ?: return session.sendAndClose(mapper, ErrorCode.ROOM_NOT_FOUND)

        log.info("SESSION: {}, ROOM: {}", principal.principal, roomId)

        val mainActor = wsDelegateActor(session, principal.principal as UserDetails)
        val actor = wsRoomActor(roomId, mainActor) // active the room actor

        sinkActor.send(SinkMessages.Listen(actor))
        session.closeStatus().awaitFirstOrNull()
        sinkActor.send(SinkMessages.Dispose(actor))

        actor.close()
        mainActor.close()
    }
}
