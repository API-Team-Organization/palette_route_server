package com.teamapi.palette.service.adapter.comfy.ws

data class ImageProgressMessage(val value: Int, val max: Int) : ComfyWSBaseMessage {
    override val type: MessageType = MessageType.IMAGE_PROGRESS
}
