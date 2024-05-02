package com.teamapi.palette.service

import com.teamapi.palette.response.ErrorCode
import com.teamapi.palette.response.exception.CustomException
import org.springframework.stereotype.Component
import org.springframework.web.server.WebSession
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@Component
class SessionHolder {
    fun current(): Mono<WebSession> {
        return Mono.deferContextual {
            Mono.just(it.get(WebSession::class.java))
        }
    }

    fun me(): Mono<Long> {
        return current()
            .flatMap {
                Mono.justOrEmpty(it.getAttribute<Long>("user"))
            }
            .switchIfEmpty { Mono.error(CustomException(ErrorCode.INVALID_SESSION)) }
    }
}