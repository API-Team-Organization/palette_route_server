package com.teamapi.palette.filter

import org.springframework.core.Ordered
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import org.springframework.web.server.WebSession
import reactor.core.publisher.Mono
import reactor.util.context.Context

@Component
class SessionAuthenticateFilter : WebFilter, Ordered {
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        return exchange.session.flatMap { session ->
            chain
                .filter(exchange)
                .contextWrite(Context.of(WebSession::class.java, session))
                .contextWrite {
                    ReactiveSecurityContextHolder.withAuthentication(
                        UsernamePasswordAuthenticationToken(
                            "${session.getAttribute<Long>("user")}", null, emptySet()
                        )
                    )
                }
        }
    }

    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE + 1
}