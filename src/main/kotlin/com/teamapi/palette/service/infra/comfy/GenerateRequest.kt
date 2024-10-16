package com.teamapi.palette.service.infra.comfy

import kotlinx.serialization.Serializable

@Serializable
data class GenerateRequest(
    val title: String,
    val pos: Int,
    val width: Int,
    val height: Int,
    val prompt: String
)
