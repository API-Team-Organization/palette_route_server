package com.teamapi.palette.dto.room

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class UpdateRoomTitleRequest (
    @field:NotNull
    @field:NotBlank
    val id: Long,
    @field:NotBlank
    val title: String
)
