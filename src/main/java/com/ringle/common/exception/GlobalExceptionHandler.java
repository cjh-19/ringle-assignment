package com.ringle.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 비즈니스 예외 처리 (도메인에서 throw한 BusinessException)
     * - 예: 이메일 중복, 사용자 없음, 수업 중복 등
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        ErrorResponse response = ErrorResponse.of(e.getCode(), e.getMessage());
        return ResponseEntity.status(e.getCode().getStatus()).body(response);
    }

    /**
     * @Valid 유효성 검증 실패 처리
     * - DTO 필드의 유효성 조건을 만족하지 못한 경우 발생
     * - FieldError 목록으로 응답
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        ErrorResponse response = ErrorResponse.of(ExceptionCode.NOT_VALID_ERROR, e.getBindingResult());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * NullPointerException 처리
     * - 예상치 못한 시스템 오류 (서비스/리포지토리 계층에서 발생 가능)
     * - 로그를 기반으로 원인 파악 후 조치 필요
     */
    @ExceptionHandler(NullPointerException.class)
    protected ResponseEntity<ErrorResponse> handleNullPointerException(NullPointerException e) {
        ErrorResponse response = ErrorResponse.of(ExceptionCode.NULL_POINT_ERROR, e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
