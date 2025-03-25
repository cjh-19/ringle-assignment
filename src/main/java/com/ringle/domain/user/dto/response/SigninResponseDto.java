package com.ringle.domain.user.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * 로그인 성공 시 반환되는 응답 DTO
 */
@Getter
@Builder
public class SigninResponseDto {
    private String role;   // STUDENT or TUTOR
    private String token;  // JWT access token
}