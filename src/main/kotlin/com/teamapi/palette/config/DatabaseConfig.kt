package com.teamapi.palette.config

import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.r2dbc.spi.ConnectionFactory
import org.jooq.impl.DefaultConfiguration
import org.jooq.impl.DefaultDSLContext
import org.springframework.boot.autoconfigure.mongo.MongoConnectionDetails
import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.r2dbc.connection.TransactionAwareConnectionFactoryProxy
import com.mongodb.reactivestreams.client.MongoClient as JMongoClient

@Configuration
@EnableMongoRepositories
@EnableConfigurationProperties(MongoProperties::class)
class DatabaseConfig {
    @Bean
    fun redisTemplateForRepository(redisConnectionFactory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, Any> {
        return ReactiveRedisTemplate(
            redisConnectionFactory,
            RedisSerializationContext
                .newSerializationContext<String, Any>()
                .key(RedisSerializer.string())
                .hashKey(RedisSerializer.string())
                .value(RedisSerializer.java())
                .hashValue(RedisSerializer.java())
                .build()
        )
    }

    @Bean
    fun mongoClient(client: JMongoClient): MongoClient = MongoClient(client)

    @Bean
    fun coroutineMongoTemplate(
        client: MongoClient, props: MongoProperties,
        connectionDetails: MongoConnectionDetails
    ): MongoDatabase = client.getDatabase(
        props.database
            ?: connectionDetails.connectionString.database
            ?: throw RuntimeException("Error while constructing mongo: No database received.")
    )

    @Bean
    fun dslContext(connectionFactory: ConnectionFactory) = DefaultDSLContext(
        DefaultConfiguration().set(TransactionAwareConnectionFactoryProxy(connectionFactory))
//                .set( // TODO: https://github.com/jOOQ/jOOQ/issues/12221
//                    Settings()
//                        .withRenderFormatted(true)
//                        .withExecuteLogging(true)
//                        .withRenderTable(RenderTable.ALWAYS)
//                )
//                .set(LoggerListener())
    )
}
