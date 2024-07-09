package com.teamapi.palette.repository

import com.teamapi.palette.entity.User
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface UserRepository : R2dbcRepository<User, Long> {
    fun existsByEmail(email: String): Mono<Boolean>

    fun findByEmail(email: String): Mono<User>
}