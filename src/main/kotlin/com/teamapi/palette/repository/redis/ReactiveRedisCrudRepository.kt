package com.teamapi.palette.repository.redis

interface ReactiveRedisCrudRepository<T, ID> {
    suspend fun <S : T> create(item: S): Boolean
    suspend fun findById(id: ID): T?
    suspend fun deleteById(id: ID)
    suspend fun delete(item: T)
}
