package com.teamapi.palette.repository.user

import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.teamapi.palette.entity.User
import com.teamapi.palette.repository.mongo.MongoDatabases
import com.teamapi.palette.repository.mongo.eq
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId
import org.springframework.stereotype.Repository

@Repository
class UserRepositoryImpl(
    database: MongoDatabase
) : UserRepository {
    private val template = database.getCollection<User>(MongoDatabases.USER)
    override suspend fun existsByEmail(email: String): Boolean {
        return template
            .find(User::email eq email)
            .limit(1)
            .count() > 0
    }

    override suspend fun findByEmail(email: String): User? {
        return template
            .find(User::email eq email)
            .limit(1)
            .firstOrNull()
    }

    override suspend fun findAll(): List<User> {
        return template.find().toList()
    }

    override suspend fun <ITEM : User> create(item: ITEM): ITEM {
        val id = template
            .insertOne(item)
            .insertedId
            ?: error("cannot create user")

        @Suppress("UNCHECKED_CAST")
        return findByIdOrNull(id.asObjectId().value) as ITEM
    }

    override suspend fun findByIdOrNull(id: ObjectId): User? {
        return template
            .find(User::id eq id)
            .firstOrNull()
    }

    override suspend fun <ITEM : User> modify(item: ITEM): Boolean {
        return template
            .replaceOne(User::id eq item.id, item)
            .modifiedCount == 1L
    }

    override suspend fun deleteById(id: ObjectId): Boolean {
        return template
            .deleteOne(User::id eq id)
            .deletedCount == 1L
    }
}
