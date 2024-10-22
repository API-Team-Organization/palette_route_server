package com.teamapi.palette.ws.dto.res

import kotlinx.serialization.Serializable

@Serializable
data class GenerateStatus(val position: Int, val generating: Boolean) : BaseResponseMessage {
    override val type: MessageType = MessageType.GENERATE_STATUS
}
