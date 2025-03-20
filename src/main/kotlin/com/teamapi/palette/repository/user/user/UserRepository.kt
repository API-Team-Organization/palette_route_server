package com.teamapi.palette.repository.user

import com.teamapi.palette.entity.User
import com.teamapi.palette.repository.mongo.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : MongoRepository<User> {
    suspend fun existsByEmail(email: String): Boolean
    suspend fun findByEmail(email: String): User?
}
