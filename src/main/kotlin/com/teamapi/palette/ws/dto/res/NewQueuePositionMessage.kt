package com.teamapi.palette.ws.dto.res

import kotlinx.serialization.Serializable

@Serializable
data class NewQueuePositionMessage(val position: Int) : BaseResponseMessage {
    override val type: MessageType = MessageType.QUEUE_POSITION_UPDATE
}
