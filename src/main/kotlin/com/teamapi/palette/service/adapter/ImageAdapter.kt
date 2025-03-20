package com.teamapi.palette.service.adapter

import com.teamapi.palette.entity.image.Image
import com.teamapi.palette.repository.image.ImageRepository
import com.teamapi.palette.response.ErrorCode
import com.teamapi.palette.response.exception.CustomException
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

@Service
class ImageAdapter(
    private val imageRepository: ImageRepository,
    private val saveAdapter: SaveAdapter,
) {
    suspend fun saveImage(data: ByteArray): Image {
        val id = ObjectId.get()
        val url = saveAdapter.saveImage(id, data)
        return imageRepository.create(
            Image(
                id = id,
                url = url
            )
        )
    }

    suspend fun deleteImage(id: String) {
        imageRepository.findByIdOrNull(ObjectId(id))?.let {
            saveAdapter.deleteImage(it)
            imageRepository.deleteById(it.id)
        }
    }

    suspend fun readImage(id: String): ByteArray = saveAdapter.readImage(getImage(id))

    suspend fun getImage(id: String): Image = imageRepository.findByIdOrNull(ObjectId(id))
        ?: throw CustomException(ErrorCode.IMAGE_NOT_FOUND)
}
