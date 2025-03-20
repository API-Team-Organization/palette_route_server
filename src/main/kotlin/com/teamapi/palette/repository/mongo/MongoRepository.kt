package com.teamapi.palette.repository.mongo

import org.bson.types.ObjectId

interface MongoRepository<T> {
    suspend fun findAll(): List<T>
    suspend fun <ITEM : T> create(item: ITEM): ITEM
    suspend fun findByIdOrNull(id: ObjectId): T?
    suspend fun <ITEM : T> modify(item: ITEM): Boolean
    suspend fun deleteById(id: ObjectId): Boolean
}
