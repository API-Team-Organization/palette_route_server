package com.teamapi.palette.repository

import com.teamapi.palette.entity.User
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : CoroutineCrudRepository<User, Long> {
    suspend fun existsByEmail(email: String): Boolean
    suspend fun findByEmail(email: String): User?
}
