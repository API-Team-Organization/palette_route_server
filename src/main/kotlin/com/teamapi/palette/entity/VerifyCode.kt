package com.teamapi.palette.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash

@RedisHash("verify")
data class VerifyCode(
    @Id
    val userId: Long,
    val code: String,
)
