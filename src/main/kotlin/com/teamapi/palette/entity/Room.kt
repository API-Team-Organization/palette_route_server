package com.teamapi.palette.entity

import org.springframework.data.annotation.Id

data class Room(
    @Id
    val id: Long? = null,
    val user: User
)
