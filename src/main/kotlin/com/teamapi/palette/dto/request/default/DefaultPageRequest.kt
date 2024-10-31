package com.teamapi.palette.dto.request.default

import kotlinx.serialization.Serializable

@Serializable
data class DefaultPageRequest(val page: Int, val size: Int)
