package com.teamapi.palette.ws.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter


@Configuration
class WebSocketConfig {
    @Bean
    fun handlerMapping(webSocketHandler: WebSocketHandler): SimpleUrlHandlerMapping {
        return SimpleUrlHandlerMapping(mapOf("/ws/**" to webSocketHandler), 1)
    }

    @Bean
    fun adapter() = WebSocketHandlerAdapter()
}
