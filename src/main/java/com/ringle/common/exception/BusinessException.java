package com.ringle.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 비즈니스 로직 예외 클래스
 * - 시스템에서 정의한 ExceptionCode를 기반으로 예외 처리
 * - 커스텀 도메인 오류(예: 중복 이메일, 비밀번호 불일치)에 사용
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ExceptionCode code;

    public BusinessException(ExceptionCode code) {
        super(code.getMessage()); // 메시지를 RuntimeException에 전달
        this.code = code;
    }
}
