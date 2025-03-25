package com.ringle.domain.user.controller;

import com.ringle.domain.user.dto.request.SigninRequestDto;
import com.ringle.domain.user.dto.request.SignupRequestDto;
import com.ringle.domain.user.dto.response.SigninResponseDto;
import com.ringle.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 인증 관련 컨트롤러 (회원가입, 로그인)
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * 회원가입 API
     *
     * @param request 회원가입 요청 데이터
     * @return 200 OK + 메시지
     */
    @Operation(summary = "회원가입", description = "학생 또는 튜터가 회원가입을 수행합니다.")
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequestDto request) {
        userService.registerUser(request);
        return ResponseEntity.ok().body(Map.of("code", 200, "message", "회원가입이 완료되었습니다."));
    }

    /**
     * 로그인 API
     *
     * @param request 로그인 요청 데이터
     * @return JWT 토큰 + 사용자 역할 반환
     */
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하고 JWT 토큰을 반환합니다.")
    @PostMapping("/signin")
    public ResponseEntity<?> signin(@Valid @RequestBody SigninRequestDto request) {
        SigninResponseDto response = userService.authenticateUser(request);
        return ResponseEntity.ok().body(Map.of("code", 200, "data", response));
    }
}
