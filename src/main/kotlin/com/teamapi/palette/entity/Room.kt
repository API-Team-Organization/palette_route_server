package com.teamapi.palette.entity

import com.teamapi.palette.response.ErrorCode
import com.teamapi.palette.response.exception.CustomException
import com.teamapi.palette.service.SessionHolder
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("tbl_room")
data class Room(
    @Id
    val id: Long? = null,
    val title: String? = "New Chat",
    @Column("user_id")
    val userId: Long
) {
    suspend fun validateUser(sessionHolder: SessionHolder): Room {
        val me = sessionHolder.me()
        if (userId != me)
            throw CustomException(ErrorCode.NOT_YOUR_ROOM)
        return this
    }
}
