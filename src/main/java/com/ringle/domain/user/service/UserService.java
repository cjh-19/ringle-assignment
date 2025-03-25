package com.ringle.domain.user.service;

import com.ringle.common.jwt.JwtUtils;
import com.ringle.domain.user.dto.request.SigninRequestDto;
import com.ringle.domain.user.dto.response.SigninResponseDto;
import com.ringle.domain.user.dto.request.SignupRequestDto;
import com.ringle.domain.user.entity.User;
import com.ringle.domain.user.repository.UserRepository;
import com.ringle.common.exception.BusinessException;
import com.ringle.common.exception.ExceptionCode;
import com.ringle.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 회원가입/로그인 관련 비즈니스 로직
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    /**
     * 회원가입 처리
     * - 이메일 중복 확인
     * - 비밀번호 일치 확인
     * - 비밀번호 암호화 후 저장
     */
    @Transactional
    public void registerUser(SignupRequestDto request) {
        // 이메일 중복 예외
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ExceptionCode.DUPLICATE_EMAIL);
        }

        // 비밂번호 미일치 예외
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new BusinessException(ExceptionCode.PASSWORD_MISMATCH);
        }

        // 비밀번호 암호화
        String encryptedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .email(request.getEmail())
                .password(encryptedPassword)
                .name(request.getName())
                .role(request.getRole())
                .build();

        userRepository.save(user);
    }

    /**
     * 로그인 처리
     * - Spring Security AuthenticationManager로 인증
     * - JWT 토큰 생성 후 반환
     */
    public SigninResponseDto authenticateUser(SigninRequestDto request) {
        // Spring Security의 AuthenticationManager를 사용한 인증
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal(); // 인증된 사용자 정보 가져오기
        String token = jwtUtils.generateToken(userDetails); // JWT 토큰 생성

        return SigninResponseDto.builder()
                .role(userDetails.getUser().getRole().name())
                .token(token)
                .build();
    }
}
