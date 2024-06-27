package com.teamapi.palette.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column

data class Room(
    @Id
    val id: Long? = null,
    val title: String? = "New Chat",
    @Column("user_id")
    val userId: Long
)
