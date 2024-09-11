package com.teamapi.palette.ws.config

import com.teamapi.palette.ws.dto.WSRoomMessage
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter
import reactor.core.publisher.Sinks
import reactor.core.publisher.Sinks.Many


@Configuration
class WebSocketConfig {
    @Bean
    fun handlerMapping(webSocketHandler: WebSocketHandler): SimpleUrlHandlerMapping {
        return SimpleUrlHandlerMapping(mapOf("/ws/**" to webSocketHandler), 1)
    }

    @Bean
    fun adapter() = WebSocketHandlerAdapter()

    @Bean
    fun sink(): Many<WSRoomMessage> {
        return Sinks.many().multicast().directBestEffort()
    }
}
