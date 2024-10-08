package com.teamapi.palette.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.session.data.redis.config.annotation.web.server.EnableRedisWebSession
import org.springframework.web.server.session.HeaderWebSessionIdResolver
import org.springframework.web.server.session.WebSessionIdResolver

@Configuration
@EnableRedisWebSession(maxInactiveIntervalInSeconds = 60 * 60 * 3) // 3h
class SessionConfig {
    companion object {
        private const val SESSION_HEADER = "x-auth-Token"
    }
    @Bean // change or no
    fun webSessionIdResolver(): WebSessionIdResolver {
        val sessionIdResolver = HeaderWebSessionIdResolver()
        sessionIdResolver.headerName = SESSION_HEADER
        return sessionIdResolver
    }
}
