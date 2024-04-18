package com.teamapi.palette.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisOperations
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig {
    @Bean
    fun redisOperations(
        factory: ReactiveRedisConnectionFactory
    ) : ReactiveRedisOperations<String, String> {
        val serializer = StringRedisSerializer()

        val builder: RedisSerializationContext.RedisSerializationContextBuilder<String, String> =
            RedisSerializationContext.newSerializationContext(StringRedisSerializer())

        val context: RedisSerializationContext<String, String> = builder.value(serializer).build()

        return ReactiveRedisTemplate(factory, context)
    }
}