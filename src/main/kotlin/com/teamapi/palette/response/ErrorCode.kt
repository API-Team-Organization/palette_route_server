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
    MAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "메일 전송 실패"),
    INVALID_VERIFY_CODE(HttpStatus.BAD_REQUEST, "잘못된 인증코드"),
    ALREADY_VERIFIED(HttpStatus.FORBIDDEN, "이미 인증 된 유저"),

    ENDPOINT_NOT_FOUND(HttpStatus.NOT_FOUND, "엔드포인트 '/%s'를 찾을 수 없음"),
    ISSUE_ON_REQUEST_BODY(HttpStatus.BAD_REQUEST, "요청 바디에서 문제 발견: %s"),
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "파라미터 '%s'(이)가 잘못 되었음"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않은 요청 메소드 '%s' (지원되는 메소드: '%s')"),
    MEDIA_TYPE_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "허용되지 않은 미디어 타입 '%s' (지원되는 타입: '%s')"),

    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 룸을 찾을 수 없음"),
    NOT_YOUR_ROOM(HttpStatus.NOT_FOUND, "해당 룸에는 접근 권한이 없음"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "'%s' 엔드포인트에 대해 접근 할 수 있는 권한이 없음"),

    CHAT_FILTERED(HttpStatus.BAD_REQUEST, "부적절한 내용 감지. 내용을 수정 해 주세요."),
    MESSAGE_TYPE_NOT_MATCH(HttpStatus.BAD_REQUEST, "적절하지 않은 메시지 유형: '%s'. 메시지 유형 '%s'이(가) 필요합니다."),
    QNA_INVALID_NOT_FULFILLED(HttpStatus.BAD_REQUEST, "모든 질문에 대해 대답하지 않았거나, 요청을 받을 수 없는 상태입니다."),
    QNA_INVALID_FULFILLED(HttpStatus.BAD_REQUEST, "이미 모든 질문에 대해 대답하였습니다."),
    QNA_INVALID_CHOICES(HttpStatus.BAD_REQUEST, "적절하지 않은 선택: '%s'. %s 선택지 중 하나가 필요합니다."),
    QNA_INVALID_GRID_CHOICES(HttpStatus.BAD_REQUEST, "적절하지 않은 그리드: (%s). 0~%d 선택지 중 하나가 필요합니다."),
    QNA_INVALID_GRID_DUPE(HttpStatus.BAD_REQUEST, "적절하지 않은 그리드: 중복이 없어야 합니다. possible dupes: (%s)"),
    QNA_INVALID_GRID_ABOVE_MAX(HttpStatus.BAD_REQUEST, "적절하지 않은 그리드: 너무 많은 선택지. 최대 크기는 '%d'입니다."),

    IMAGE_NOT_FOUND(HttpStatus.BAD_REQUEST, "'%s'에 해당하는 이미지를 찾을 수 없습니다."),
    ;

    override fun getName() = name
}
