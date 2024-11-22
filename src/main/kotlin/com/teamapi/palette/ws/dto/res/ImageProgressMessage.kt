package com.teamapi.palette.ws.dto.res

import kotlinx.serialization.Serializable

@Serializable
data class ImageProgressMessage(val value: Int, val max: Int) : BaseResponseMessage {
    override val type: MessageType = MessageType.IMAGE_PROGRESS
}
