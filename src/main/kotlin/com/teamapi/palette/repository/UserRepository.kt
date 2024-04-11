package com.teamapi.palette.repository

import com.teamapi.palette.entity.User
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : R2dbcRepository<User, Long>