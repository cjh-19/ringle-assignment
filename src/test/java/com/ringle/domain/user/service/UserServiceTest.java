package com.ringle.domain.user.service;

import com.ringle.common.exception.BusinessException;
import com.ringle.common.exception.ExceptionCode;
import com.ringle.common.jwt.JwtUtils;
import com.ringle.domain.user.dto.request.SigninRequestDto;
import com.ringle.domain.user.dto.request.SignupRequestDto;
import com.ringle.domain.user.dto.response.SigninResponseDto;
import com.ringle.domain.user.entity.User;
import com.ringle.domain.user.entity.enums.Role;
import com.ringle.domain.user.repository.UserRepository;
import com.ringle.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private BCryptPasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private JwtUtils jwtUtils;
    private UserService userService;

    @BeforeEach
    void setup() {
        // 각 의존성들을 Mockito로 mocking
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(BCryptPasswordEncoder.class);
        authenticationManager = mock(AuthenticationManager.class);
        jwtUtils = mock(JwtUtils.class);

        // 테스트 대상 서비스 인스턴스 생성
        userService = new UserService(userRepository, passwordEncoder, authenticationManager, jwtUtils);
    }

    /**
     * [정상 회원가입 테스트]
     * - 이메일이 중복되지 않고 비밀번호가 일치할 때
     * - 암호화된 비밀번호로 유저를 저장하는 시나리오
     */
    @Test
    void registerUser_정상회원가입_성공() {
        // given: 정상 요청 DTO 설정
        SignupRequestDto request = new SignupRequestDto();
        request.setEmail("test@ringle.com");
        request.setName("테스터");
        request.setPassword("1234");
        request.setPasswordConfirm("1234");
        request.setRole(Role.STUDENT);

        // 이메일 중복 없음
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        // 비밀번호 암호화 mock
        when(passwordEncoder.encode("1234")).thenReturn("encoded1234");

        // when: 회원가입 메서드 호출
        userService.registerUser(request);

        // then: 저장된 User 객체의 필드 검증
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo("test@ringle.com");
        assertThat(savedUser.getPassword()).isEqualTo("encoded1234");
        assertThat(savedUser.getRole()).isEqualTo(Role.STUDENT);
    }

    /**
     * [회원가입 예외 테스트]
     * - 이미 등록된 이메일일 경우 예외 발생
     */
    @Test
    void registerUser_이메일중복시_예외() {
        // given: 중복 이메일 설정
        SignupRequestDto request = new SignupRequestDto();
        request.setEmail("exist@ringle.com");
        request.setPassword("1234");
        request.setPasswordConfirm("1234");

        // 이메일 존재 여부 mock
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // then: 예외 발생 검증
        assertThatThrownBy(() -> userService.registerUser(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ExceptionCode.DUPLICATE_EMAIL.getMessage());
    }

    /**
     * [회원가입 예외 테스트]
     * - 비밀번호와 비밀번호 확인이 다를 경우 예외 발생
     */
    @Test
    void registerUser_비밀번호불일치시_예외() {
        // given: 비밀번호와 확인 불일치
        SignupRequestDto request = new SignupRequestDto();
        request.setEmail("test@ringle.com");
        request.setPassword("1234");
        request.setPasswordConfirm("4321");

        // 이메일은 존재하지 않음
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);

        // then: 예외 발생 검증
        assertThatThrownBy(() -> userService.registerUser(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ExceptionCode.PASSWORD_MISMATCH.getMessage());
    }

    /**
     * [정상 로그인 테스트]
     * - 인증 성공 시 JWT 토큰과 유저 역할 반환
     */
    @Test
    void authenticateUser_정상로그인_성공() {
        // given: 로그인 요청 DTO 설정
        SigninRequestDto request = new SigninRequestDto();
        request.setEmail("login@ringle.com");
        request.setPassword("pass");

        // 인증된 사용자 정보 생성
        User user = User.builder().id(1L).email("login@ringle.com").role(Role.TUTOR).build();
        CustomUserDetails userDetails = new CustomUserDetails(user);

        // 인증 객체 mock
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // 인증 처리 시 인증 객체 반환
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtUtils.generateToken(userDetails)).thenReturn("mocked-jwt-token");

        // when: 로그인 서비스 호출
        SigninResponseDto response = userService.authenticateUser(request);

        // then: 응답 토큰 및 역할 검증
        assertThat(response.getToken()).isEqualTo("mocked-jwt-token");
        assertThat(response.getRole()).isEqualTo("TUTOR");
    }
}
