package com.teamapi.palette.service

import com.teamapi.palette.service.adapter.ImageAdapter
import org.springframework.stereotype.Service

@Service
class ImageService(
    private val imageAdapter: ImageAdapter,
) {
    suspend fun readImage(id: String): ByteArray {
        return imageAdapter.readImage(id)
    }
}
