package com.teamapi.palette.response

import org.springframework.http.HttpStatus

interface ResponseCode {
    val message: String
    fun getName(): String
    val statusCode: HttpStatus
}