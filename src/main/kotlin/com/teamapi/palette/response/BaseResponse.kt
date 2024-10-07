package com.teamapi.palette.response

import kotlinx.serialization.Serializable

@Serializable
sealed interface BaseResponse {
    val code: Int
    val message: String
}
