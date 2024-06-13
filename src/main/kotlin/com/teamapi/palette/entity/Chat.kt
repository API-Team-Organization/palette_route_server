package com.teamapi.palette.entity

import org.springframework.data.annotation.Id

data class Chat(
    @Id
    val id: Long? = null,
    val message: String
)
