package com.teamapi.palette.service.adapter.save

import com.teamapi.palette.entity.image.Image
import com.teamapi.palette.service.adapter.SaveAdapter
import com.teamapi.palette.util.guessExtension
import org.bson.types.ObjectId
import org.springframework.stereotype.Component
import kotlin.io.path.*

@Component
class LocalSaveAdapter : SaveAdapter {
    private val savePath = Path("images").apply {
        if (!exists())
            createDirectories()
    }

    override suspend fun saveImage(id: ObjectId, bytes: ByteArray): String {
        val path = Path(savePath.absolutePathString(), "$id.${guessExtension(bytes)}")
        path.createFile()
        path.writeBytes(bytes)

        return path.absolutePathString()
    }

    override suspend fun readImage(image: Image): ByteArray {
        val path = Path(image.url)
        return path.readBytes()
    }

    override suspend fun deleteImage(image: Image) {
        val path = Path(image.url)
        path.deleteIfExists()
    }
}
