package com.teamapi.palette.filter

import com.teamapi.palette.response.ErrorCode
import com.teamapi.palette.response.exception.CustomException
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisOperations
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class SessionAuthenticateFilter(
    private val factory: ReactiveRedisConnectionFactory,
    private val sessionOps: ReactiveRedisOperations<String, String>,
    private val userDetailsService: UserDetailsService
) : WebFilter {
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val session = exchange.request.cookies.getValue("_session")

        val sessionUUID = session.firstOrNull()

        if (sessionUUID != null) {
            return factory
                .reactiveConnection
                .serverCommands()
                .flushAll()
                .thenMany(
                    sessionOps.keys(sessionUUID.value)
                        .flatMap { sessionOps.opsForValue().get(it) }
                )
                .switchIfEmpty { Flux.error<Void>(CustomException(ErrorCode.USER_NOT_FOUND)) } // TODO: fix not be handled
                .map {
                    val user = userDetailsService.loadUserByUsername(it)
                    SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(user, it, listOf())
                }
                .then(
                    chain.filter(exchange)
                )
        }
        return chain.filter(exchange)
    }
}