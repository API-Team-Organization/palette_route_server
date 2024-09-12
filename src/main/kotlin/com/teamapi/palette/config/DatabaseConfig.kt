package com.teamapi.palette.config

import io.r2dbc.spi.ConnectionFactory
import org.jooq.SQLDialect
import org.jooq.impl.DefaultConfiguration
import org.jooq.impl.DefaultDSLContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories
import org.springframework.r2dbc.connection.TransactionAwareConnectionFactoryProxy


@Configuration
@EnableRedisRepositories
class DatabaseConfig {
    @Bean
    fun redisTemplate(redisConnectionFactory: RedisConnectionFactory): RedisTemplate<*, *> {
        val template = RedisTemplate<String, String>()
        template.connectionFactory = redisConnectionFactory
        return template
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
