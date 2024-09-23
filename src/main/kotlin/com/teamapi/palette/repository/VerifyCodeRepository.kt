package com.teamapi.palette.repository

import com.teamapi.palette.entity.VerifyCode
import com.teamapi.palette.repository.redis.ReactiveRedisCrudRepository
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.data.redis.core.*
import org.springframework.stereotype.Repository

@Repository
class VerifyCodeRepository(private val redisOperations: ReactiveRedisTemplate<String, Any>) : ReactiveRedisCrudRepository<VerifyCode, Long> {
    private val namespace: String = generateNamespace<VerifyCode>()

    override suspend fun <S : VerifyCode> create(item: S): Boolean {
        val kv = redisOperations.opsForValue()
        val saveKey = "$namespace${item.userId}"
        if (redisOperations.hasKeyAndAwait(saveKey)) { // override
            deleteById(item.userId)
        }

        return kv.setAndAwait(saveKey, item)
    }

    override suspend fun findById(id: Long): VerifyCode? {
        val kv = redisOperations.opsForValue()
        println(kv.get("$namespace$id").awaitFirstOrNull())
        return kv.getAndAwait("$namespace$id") as? VerifyCode
    }

    override suspend fun deleteById(id: Long) {
        redisOperations.deleteAndAwait("$namespace$id")
    }

    override suspend fun delete(item: VerifyCode) {
        deleteById(item.userId)
    }
}

inline fun <reified T : Any> generateNamespace(): String {
    val rh = T::class.annotations.find { it is RedisHash } as? RedisHash
        ?: throw Error("Entity class '${T::class.simpleName}' is not annotated with RedisHash.")
    return "palette:${rh.value.trim()}:"
}
