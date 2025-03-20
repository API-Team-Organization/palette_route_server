package com.teamapi.palette.service.adapter

import com.teamapi.palette.service.adapter.comfy.GenerateRequest
import com.teamapi.palette.service.adapter.comfy.ws.ComfyWSBaseMessage
import kotlinx.coroutines.flow.Flow

interface GenerativeImageAdapter {
    suspend fun draw(prompt: GenerateRequest): Flow<ComfyWSBaseMessage>
}
