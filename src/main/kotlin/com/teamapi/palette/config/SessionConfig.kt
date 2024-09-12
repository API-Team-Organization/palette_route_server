package com.teamapi.palette.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.session.data.redis.config.annotation.web.server.EnableRedisWebSession
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.session.WebSessionIdResolver

@Configuration
@EnableRedisWebSession(maxInactiveIntervalInSeconds = 60 * 60 * 3) // 3h
class SessionConfig {
    companion object {
        private const val SESSION_HEADER = "x-auth-token"
    }
    @Bean // change or no
    fun webSessionIdResolver(): WebSessionIdResolver {
        val sessionIdResolver = object : WebSessionIdResolver {

            override fun resolveSessionIds(exchange: ServerWebExchange): MutableList<String> {
                val headers = exchange.request.headers
                val h = headers.getOrDefault(SESSION_HEADER, headers.getOrEmpty("Sec-WebSocket-Protocol" /* WS Hack */))
                println(h)
                return h
            }

            override fun setSessionId(exchange: ServerWebExchange, sessionId: String) {
                exchange.response.headers.set(SESSION_HEADER, sessionId)
            }

            override fun expireSession(exchange: ServerWebExchange) {
                setSessionId(exchange, "")
            }

        }
        return sessionIdResolver
    }
}
