package com.teamapi.palette.service.adapter.comfy.ws

import kotlinx.serialization.Serializable

@Serializable
data class ImageProgressMessage(val value: Int, val max: Int) : ComfyWSBaseMessage {
    override val type: MessageType = MessageType.IMAGE_PROGRESS
}
