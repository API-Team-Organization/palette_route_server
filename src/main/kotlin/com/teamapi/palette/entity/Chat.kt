package com.teamapi.palette.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("tbl_chat")
data class Chat(
    @Id
    val id: Long? = null,
    val message: String,
    val resource: String = "CHAT",
    val datetime: LocalDateTime,
    @Column("room_id")
    val roomId: Long,
    @Column("user_id")
    val userId: Long,
    @Column("is_ai")
    val isAi: Boolean
)
