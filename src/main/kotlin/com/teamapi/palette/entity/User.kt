package com.teamapi.palette.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import java.util.Date

data class User(
    val email: String,
    val password: String, // hashed
    val username: String,

    @Column("birth_date")
    val birthDate: Date,

    @Id
    val id: Long = -1L
)