package com.teamapi.palette.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import java.time.Instant

data class Chat(
    @Id
    val id: Long? = null,
    val message: String,
    val datetime: Instant,
    @Column("room_id")
    val roomId: Long,
    @Column("user_id")
    val userId: Long,
    @Column("is_ai")
    val isAi: Boolean
)
