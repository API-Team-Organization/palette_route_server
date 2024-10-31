package com.teamapi.palette.service.adapter

import com.azure.core.util.BinaryData
import com.azure.storage.blob.BlobAsyncClient
import com.azure.storage.blob.BlobServiceAsyncClient
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.stereotype.Component
import java.util.*

@Component
class BlobSaveAdapter(
    private val blob: BlobServiceAsyncClient,
) {
    suspend fun save(file: ByteArray): BlobAsyncClient {
        val space = blob.getBlobContainerAsyncClient("palette")
        val blobClient = space.getBlobAsyncClient("${UUID.randomUUID()}.png")
        blobClient.upload(BinaryData.fromBytes(file)).awaitSingle()
        return blobClient
    }
}
