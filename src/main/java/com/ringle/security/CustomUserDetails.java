package com.ringle.security;

import com.ringle.domain.user.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security에서 사용자 인증 정보를 담는 객체
 * - User 엔티티를 감싸면서 UserDetails 인터페이스를 구현
 * - 인증 후 SecurityContext에 저장됨
 */
@Getter
public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    /**
     * 인증된 사용자 ID 반환 (커스텀 필드)
     * - 서비스 단에서 사용자 ID를 사용할 수 있도록 제공
     */
    public Long getUserId() {
        return user.getId();
    }

    /**
     * 사용자 권한 목록 반환
     * - Spring Security는 "ROLE_" 접두어가 필수
     * - Enum(Role.STUDENT, TUTOR)을 문자열로 변환
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(() -> "ROLE_" + user.getRole().name());
    }

    /**
     * 사용자 비밀번호 반환
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * 사용자 식별자 (로그인 ID) 반환
     * - 여기선 email 사용
     */
    @Override
    public String getUsername() {
        return user.getEmail();
    }
}
