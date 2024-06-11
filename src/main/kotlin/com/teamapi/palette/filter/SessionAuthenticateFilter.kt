package com.teamapi.palette.filter

import org.springframework.core.Ordered
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class SessionAuthenticateFilter : WebFilter, Ordered {
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        return chain.filter(exchange)
    }

    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE + 1
}
