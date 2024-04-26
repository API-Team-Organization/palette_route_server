package com.teamapi.palette.config

import org.springframework.context.annotation.Configuration
import org.springframework.session.data.redis.config.annotation.web.server.EnableRedisWebSession

@Configuration
@EnableRedisWebSession(maxInactiveIntervalInSeconds = 60 * 60 * 3) // 3h
class SessionConfig {
//    @Bean // change or no
//    fun webSessionIdResolver(): WebSessionIdResolver {
//        val sessionIdResolver = HeaderWebSessionIdResolver()
//        sessionIdResolver.headerName = "X-AUTH-TOKEN" // Define Session Header Name
//        return sessionIdResolver
//    }
}