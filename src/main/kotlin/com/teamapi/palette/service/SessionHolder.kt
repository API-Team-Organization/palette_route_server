package com.teamapi.palette.service

import org.springframework.stereotype.Component
import org.springframework.web.server.WebSession
import reactor.core.publisher.Mono

@Component
class SessionHolder {
    fun current(): Mono<WebSession> {
        return Mono.deferContextual {
            Mono.just(it.get(WebSession::class.java))
        }
    }
}