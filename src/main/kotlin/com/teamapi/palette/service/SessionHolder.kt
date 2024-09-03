package com.teamapi.palette.service

import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository.DEFAULT_SPRING_SECURITY_CONTEXT_ATTR_NAME
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebSession
import reactor.core.publisher.Mono

@Component
class SessionHolder {
    private fun getServerWebExchange(): Mono<ServerWebExchange> {
        return Mono.deferContextual {
            Mono.just(it.get(ServerWebExchange::class.java))
        }
    }

    fun getWebSession(): Mono<WebSession> {
        return getServerWebExchange()
            .flatMap { exchange: ServerWebExchange -> exchange.session }
    }

    fun getSecurityContext(): Mono<SecurityContext> {
        return getWebSession()
            .flatMap { session: WebSession ->
                ReactiveSecurityContextHolder.getContext()
                    .switchIfEmpty(Mono.fromSupplier {
                        SecurityContextImpl().also { session.attributes[DEFAULT_SPRING_SECURITY_CONTEXT_ATTR_NAME] = it }
                    })
            }
    }

    fun userInfo(): Mono<UserDetails> {
        return getSecurityContext()
            .map { it.authentication.principal }
            .cast(UserDetails::class.java)
    }

    fun me(): Mono<Long> {
        return userInfo().map { it.username.toLong() }
    }
}
