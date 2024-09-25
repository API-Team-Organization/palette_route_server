package com.teamapi.palette.entity.chat

import com.teamapi.palette.entity.consts.PromptType
import kotlinx.serialization.Serializable

@Serializable
sealed class PromptData(val type: PromptType) {
    @Serializable
    data class Selectable(val choice: List<String>) : PromptData(PromptType.SELECTABLE)
    @Serializable
    data class Grid(val xSize: Int, val ySize: Int) : PromptData(PromptType.GRID)
    @Serializable
    data object UserInput : PromptData(PromptType.USER_INPUT)
}
