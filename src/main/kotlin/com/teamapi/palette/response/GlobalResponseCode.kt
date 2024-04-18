package com.teamapi.palette.response

import org.springframework.http.HttpStatus

enum class GlobalResponseCode(
    override val statusCode: HttpStatus,
    override val message: String
) : ResponseCode {
    USER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 같은 사용자가 존재함"),
    INTERNAL_SERVER_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "내부 서버 에러"),
    ;

    override fun getName() = name
}