package com.ringle.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExceptionCode {

    // 유저 관련 예외
    USER_NOT_FOUND(404, "USER_001", "사용자를 찾을 수 없습니다."),
    DUPLICATE_EMAIL(409, "USER_002", "이미 사용 중인 이메일입니다."),
    PASSWORD_MISMATCH(400, "USER_003", "비밀번호가 일치하지 않습니다."),
    TUTOR_AVAILABILITY_NOT_FOUND(404, "TUTOR_001", "수업 가능한 튜터가 없습니다."),

    // 수업 조회 예외
    DATE_IN_THE_PAST(400, "DATE_001", "과거 날짜는 예약할 수 없습니다."),

    // 수업 가능 시간 등록 / 삭제 예외
    INVALID_START_TIME(400, "AVAILABILITY_001", "시작 시간은 정각 또는 30분이어야 합니다."),
    ALREADY_BOOKED(409, "AVAILABILITY_002", "이미 예약된 시간은 삭제할 수 없습니다."),
    AVAILABILITY_TIME_PASSED(400, "AVAILABILITY_003", "현재 시간 이전의 시간은 등록할 수 없습니다."),
    AVAILABILITY_NOT_FOUND(404, "AVAILABILITY_004", "수업 가능 시간을 찾을 수 없습니다."),
    AVAILABILITY_UNAUTHORIZED(403, "AVAILABILITY_005", "본인의 수업 시간만 삭제할 수 있습니다."),

    // 시스템 오류
    NULL_POINT_ERROR(500, "SYS_001", "NullPointerException 발생"),

    // 요청 값 유효성 검증 실패
    NOT_VALID_ERROR(400, "SYS_002", "유효성 검증 오류");

    /**
     * status: HTTP 응답 코드
     * code: 시스템 내부용 오류 코드 (카테고리/상태 파악에 유용)
     * message: 사용자에게 제공할 메시지
     */
    private final int status;
    private final String code;
    private final String message;
}
