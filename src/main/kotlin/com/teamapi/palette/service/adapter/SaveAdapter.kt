package com.teamapi.palette.service.adapter

import com.teamapi.palette.entity.image.Image
import org.bson.types.ObjectId

interface SaveAdapter {
    suspend fun saveImage(id: ObjectId, bytes: ByteArray): String
    suspend fun readImage(image: Image): ByteArray
    suspend fun deleteImage(image: Image)
}
