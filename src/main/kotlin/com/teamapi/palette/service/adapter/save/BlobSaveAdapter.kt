package com.teamapi.palette.service.adapter.save

import com.azure.core.util.BinaryData
import com.azure.storage.blob.BlobServiceAsyncClient
import com.teamapi.palette.entity.image.Image
import com.teamapi.palette.service.adapter.SaveAdapter
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.bson.types.ObjectId

class BlobSaveAdapter(
    private val blob: BlobServiceAsyncClient,
) : SaveAdapter {
    override suspend fun saveImage(id: ObjectId, bytes: ByteArray): String {
        val space = blob.getBlobContainerAsyncClient("palette")
        val blobClient = space.getBlobAsyncClient("$id.png")
        blobClient.upload(BinaryData.fromBytes(bytes)).awaitSingle()
        return blobClient.blobUrl
    }

    override suspend fun readImage(image: Image): ByteArray {
        val space = blob.getBlobContainerAsyncClient("palette")
        val blobClient = space.getBlobAsyncClient("${image.id}.png")
        return blobClient.downloadContent().awaitSingle().toBytes()
    }

    override suspend fun deleteImage(image: Image) {
        val space = blob.getBlobContainerAsyncClient("palette")
        val blobClient = space.getBlobAsyncClient("${image.id}.png")
        blobClient.deleteIfExists().awaitSingleOrNull()
    }
}

