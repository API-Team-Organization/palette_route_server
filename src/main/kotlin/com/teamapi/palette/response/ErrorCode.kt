package com.teamapi.palette.response

import org.springframework.http.HttpStatus

enum class ErrorCode(
    override val statusCode: HttpStatus,
    override val message: String
) : ResponseCode {
    USER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 같은 사용자가 존재함"),
    USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "해당 유저를 찾을 수 없음"),
    INVALID_SESSION(HttpStatus.UNAUTHORIZED, "잘못된 세션"),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 틀림"),
    INTERNAL_SERVER_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "내부 서버 에러"),
    ENDPOINT_NOT_FOUND(HttpStatus.NOT_FOUND, "엔드포인트를 찾을 수 없음"),
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "잘못된 파라미터"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않은 요청 메소드"),
    MEDIA_TYPE_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "허용되지 않은 미디어 자료형"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "올바르지 않은 요청")
    ;

    override fun getName() = name
}