package com.teamapi.palette.filter

import com.teamapi.palette.response.ErrorCode
import com.teamapi.palette.response.exception.CustomException
import org.springframework.core.Ordered
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import org.springframework.web.server.WebSession
import org.springframework.web.util.pattern.PathPatternParser
import reactor.core.publisher.Mono
import reactor.util.context.Context

@Component
class SessionAuthenticateFilter : WebFilter, Ordered {
    // TODO: Automatically sync with ant filter
    private val allowedPath = arrayListOf(
        PathPatternParser.defaultInstance.parse("/auth/**")
    )

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        if (allowedPath.any { it.matches(exchange.request.path.pathWithinApplication()) }) {
            return exchange.session.flatMap { session ->
                chain
                    .filter(exchange)
                    .contextWrite(Context.of(WebSession::class.java, session))
            }
        } // else
        return exchange.session
            .doOnNext {
                it.getAttribute<Long>("user") ?: throw CustomException(ErrorCode.INVALID_SESSION)
            }
            .then(
                exchange.session.flatMap { session ->
                    chain
                        .filter(exchange)
                        .contextWrite(Context.of(WebSession::class.java, session))
                }
            )
    }

    override fun getOrder(): Int = 0
}