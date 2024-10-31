package com.teamapi.palette.dto.request.room

import jakarta.validation.constraints.NotBlank
import kotlinx.serialization.Serializable

@Serializable
data class UpdateRoomTitleRequest (
    @field:NotBlank
    val title: String
)
