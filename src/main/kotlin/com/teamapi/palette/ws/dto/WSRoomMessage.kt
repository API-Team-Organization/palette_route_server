package com.teamapi.palette.ws.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.teamapi.palette.entity.Chat

data class WSRoomMessage @JsonCreator constructor(
    val roomId: Long,
    val action: RoomAction,
    val message: Chat?, // change to nullable if needed
)

enum class RoomAction {
    START, TEXT, IMAGE, PROMPT, END
}
