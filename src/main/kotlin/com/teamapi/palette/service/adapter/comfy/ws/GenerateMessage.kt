package com.teamapi.palette.service.adapter.comfy.ws

import kotlinx.serialization.Serializable

@Serializable
data class GenerateMessage(
    val result: Boolean,
    val image: String? = null,
    val error: String? = null,
    override val type: MessageType = MessageType.GENERATE_FINISH
) : ComfyWSBaseMessage
