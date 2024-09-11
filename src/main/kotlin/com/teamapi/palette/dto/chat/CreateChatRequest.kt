package com.teamapi.palette.dto.chat

import com.fasterxml.jackson.annotation.JsonCreator

data class CreateChatRequest @JsonCreator constructor(
    val message: String,
)
