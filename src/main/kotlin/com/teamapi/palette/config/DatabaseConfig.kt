package com.teamapi.palette.config

import io.r2dbc.spi.ConnectionFactory
import org.jooq.SQLDialect
import org.jooq.impl.DefaultConfiguration
import org.jooq.impl.DefaultDSLContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.r2dbc.connection.TransactionAwareConnectionFactoryProxy

@Configuration
class DatabaseConfig {
    @Bean
    fun redisTemplateForRepository(redisConnectionFactory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, Any> {
        return ReactiveRedisTemplate(redisConnectionFactory, RedisSerializationContext
            .newSerializationContext<String, Any>()
            .key(RedisSerializer.string())
            .hashKey(RedisSerializer.string())
            .value(RedisSerializer.java())
            .hashValue(RedisSerializer.java())
            .build()
        )
    }

    @Bean
    fun dslContext(connectionFactory: ConnectionFactory) = DefaultDSLContext(
        DefaultConfiguration().set(TransactionAwareConnectionFactoryProxy(connectionFactory))
            .set(SQLDialect.MARIADB) // TODO: https://github.com/jOOQ/jOOQ/issues/12221
//                .set(
//                    Settings()
//                        .withRenderFormatted(true)
//                        .withExecuteLogging(true)
//                        .withRenderTable(RenderTable.ALWAYS)
//                )
//                .set(LoggerListener())
    )
}
