package com.teamapi.palette.service.adapter.comfy.ws

import kotlinx.serialization.Serializable

@Serializable
data class QueueInfoMessage(
    val position: Int,
    override val type: MessageType = MessageType.QUEUE_STATUS
) : ComfyWSBaseMessage
