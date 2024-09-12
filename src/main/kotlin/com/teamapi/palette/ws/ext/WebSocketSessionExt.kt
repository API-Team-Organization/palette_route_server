package com.teamapi.palette.ws.ext

import com.fasterxml.jackson.databind.ObjectMapper
import com.teamapi.palette.response.ErrorCode
import com.teamapi.palette.ws.dto.res.ErrorMessage
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.web.reactive.socket.CloseStatus
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.kotlin.core.publisher.toMono

suspend fun WebSocketSession.sendAndClose(mapper: ObjectMapper, error: ErrorCode) {
    val msg = textMessage(mapper.writeValueAsString(ErrorMessage.of(error))).toMono()
    send(msg).awaitSingleOrNull()

    close(CloseStatus.BAD_DATA).awaitSingleOrNull()
}
