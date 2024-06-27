package com.teamapi.palette.response

import org.springframework.http.HttpStatus

enum class ErrorCode(
    override val statusCode: HttpStatus,
    override val message: String
) : ResponseCode {
    USER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 같은 사용자가 존재함"),
    USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "해당 유저를 찾을 수 없음"),
    INVALID_SESSION(HttpStatus.UNAUTHORIZED, "잘못된 세션"),
    INVALID_CREDENTIALS(HttpStatus.BAD_REQUEST, "잘못된 로그인 정보"),
    INTERNAL_SERVER_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "내부 서버 에러"),

    ENDPOINT_NOT_FOUND(HttpStatus.NOT_FOUND, "엔드포인트 '/%s'를 찾을 수 없음"),
    ISSUE_ON_REQUEST_BODY(HttpStatus.BAD_REQUEST, "요청 바디에서 문제 발견: %s"),
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "파라미터 '%s'(이)가 잘못 되었음"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않은 요청 메소드 '%s' (지원되는 메소드: '%s')"),
    MEDIA_TYPE_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "허용되지 않은 미디어 타입 '%s' (지원되는 타입: '%s')"),

    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 룸을 찾을 수 없음"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한 없음"),

    CHAT_FILTERED(HttpStatus.BAD_REQUEST, "부적절한 내용 감지. 내용을 수정 해 주세요.")
    ;

    override fun getName() = name
}
