package com.teamapi.palette.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig {
    @Bean
    fun client() = WebClient.builder().exchangeStrategies(
        ExchangeStrategies.builder()
            .codecs { it.defaultCodecs().maxInMemorySize(30 * 1024 * 1024) }
            .build()
    ).build()
}
