package com.ringle.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BusinessException extends RuntimeException {

    /**
     * 비즈니스 로직에서 발생시키는 예외 클래스
     * - ExceptionCode를 포함하여 통합된 예외 응답 생성 가능
     * - 커스텀 도메인 오류(예: 중복 이메일, 비밀번호 불일치)에 사용
     */
    private final ExceptionCode code;
}
