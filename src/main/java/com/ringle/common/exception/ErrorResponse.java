package com.ringle.common.exception;

import lombok.*;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ErrorResponse {

    private int status;
    private String code;
    private String message;
    private String reason;
    private List<FieldErrorResponse> errors = new ArrayList<>();

    /**
     * 예외 코드 + 상세 메시지를 담은 응답 객체 생성
     * - 주로 BusinessException 발생 시 사용
     */
    @Builder
    public ErrorResponse(final ExceptionCode code, final String reason) {
        this.status = code.getStatus();
        this.code = code.getCode();
        this.message = code.getMessage();
        this.reason = reason;
    }

    /**
     * 유효성 검증 실패 시 필드별 오류 목록을 포함한 응답 생성
     */
    @Builder
    public ErrorResponse(final ExceptionCode code, final List<FieldErrorResponse> errors) {
        this.status = code.getStatus();
        this.code = code.getCode();
        this.message = code.getMessage();
        this.errors = errors;
    }

    // BusinessException 등 단순 오류
    public static ErrorResponse of(final ExceptionCode code, final String reason) {
        return new ErrorResponse(code, reason);
    }

    // @Valid 오류 → BindingResult로 필드별 상세 오류 추출
    public static ErrorResponse of(final ExceptionCode code, final BindingResult bindingResult) {
        List<FieldErrorResponse> fieldErrors = bindingResult.getFieldErrors().stream()
                .map(error -> new FieldErrorResponse(
                        error.getField(),
                        error.getRejectedValue() != null ? error.getRejectedValue().toString() : "",
                        error.getDefaultMessage()))
                .collect(Collectors.toList());

        return new ErrorResponse(code, fieldErrors);
    }

    /**
     * 필드별 유효성 검증 실패 정보를 담는 서브 클래스
     * - field: 오류가 발생한 필드명
     * - value: 입력된 값
     * - reason: 오류 사유 (message)
     */
    @Getter
    @AllArgsConstructor
    public static class FieldErrorResponse {
        private String field;
        private String value;
        private String reason;
    }
}
