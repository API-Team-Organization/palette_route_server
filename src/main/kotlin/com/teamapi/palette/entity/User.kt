package com.teamapi.palette.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import java.time.LocalDate

data class User(
    val email: String,
    val password: String, // hashed
    val username: String,

    @Column("birth_date")
    val birthDate: LocalDate,

    @Id
    val id: Long? = null
)
