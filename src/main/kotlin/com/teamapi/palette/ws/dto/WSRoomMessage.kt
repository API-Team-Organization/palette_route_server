package com.teamapi.palette.ws.dto

import com.teamapi.palette.ws.dto.res.BaseResponseMessage
import org.bson.types.ObjectId

data class WSRoomMessage(
    val roomId: ObjectId,
    val message: BaseResponseMessage, // change to nullable if needed
)
