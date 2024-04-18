package com.teamapi.palette.response

import org.springframework.http.HttpStatus

enum class GlobalResponseCode(
    override val statusCode: HttpStatus,
    override val message: String
) : ResponseCode {
    USER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 같은 사용자가 존재함"),
    INTERNAL_SERVER_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "내부 서버 에러"),
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "잘못된 파라미터"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않은 요청 메소드"),
    MEDIA_TYPE_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "허용되지 않은 미디어 자료형"),

    ;

    override fun getName() = name
}