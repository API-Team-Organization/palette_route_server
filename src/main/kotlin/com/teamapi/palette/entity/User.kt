package com.teamapi.palette.entity

import org.springframework.data.annotation.Id

data class User(
    val email: String,
    val password: String, // hashed

    @Id
    val id: Long = -1L
)