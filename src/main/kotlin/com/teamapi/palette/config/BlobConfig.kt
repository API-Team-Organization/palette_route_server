package com.teamapi.palette.config

import com.azure.storage.blob.BlobContainerAsyncClient
import com.azure.storage.blob.BlobServiceAsyncClient
import com.azure.storage.blob.BlobServiceClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BlobConfig (
    @Value("\${spring.cloud.azure.storage.blob.account-key}")
    private val sasToken: String,
    @Value("\${spring.cloud.azure.storage.blob.endpoint}")
    private val endpoint: String
) {
    @Bean
    fun blobServiceClient(): BlobServiceAsyncClient? {
        return BlobServiceClientBuilder()
            .sasToken(sasToken)
            .endpoint(endpoint)
            .buildAsyncClient()
    }
}