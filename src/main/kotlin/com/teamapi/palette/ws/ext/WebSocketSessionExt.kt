package com.teamapi.palette.ws.ext

import com.teamapi.palette.response.ErrorCode
import com.teamapi.palette.ws.dto.res.ErrorMessage
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.web.reactive.socket.CloseStatus
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.kotlin.core.publisher.toMono

suspend fun WebSocketSession.sendAndClose(mapper: Json, error: ErrorCode) {
    val msg = textMessage(mapper.encodeToString(ErrorMessage.of(error))).toMono()
    send(msg).awaitSingleOrNull()

    close(CloseStatus.BAD_DATA).awaitSingleOrNull()
}
