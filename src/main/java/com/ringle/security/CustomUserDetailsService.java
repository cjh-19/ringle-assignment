package com.ringle.security;

import com.ringle.domain.user.entity.User;
import com.ringle.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * UserDetailsService 구현체
 * - Spring Security가 로그인 시 사용자 정보를 조회하기 위해 사용하는 서비스
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    /**
     * 이메일(email)을 기반으로 사용자 조회
     * - 사용자 없으면 UsernameNotFoundException 예외 발생 (Spring Security 내부 처리용)
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email);
        if (user == null) throw new UsernameNotFoundException("등록된 사용자가 없습니다.");
        return new CustomUserDetails(user);
    }
}
