package com.ringle.common.jwt;

import com.ringle.domain.user.entity.User;
import com.ringle.security.CustomUserDetails;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtUtils {

    private final SecretKey hmacKey;
    private final Long expirationTime;
    private final String issuer;

    /**
     * application.yml에서 설정 값 로드 후 JWT 관련 필드 초기화
     */
    public JwtUtils(Environment env) {
        this.hmacKey = Keys.hmacShaKeyFor(env.getProperty("jwt.secret-key").getBytes());
        this.expirationTime = Long.parseLong(env.getProperty("jwt.access_expiration"));
        this.issuer = env.getProperty("jwt.issuer");
    }

    /**
     * 인증된 사용자 정보를 기반으로 JWT 액세스 토큰 생성
     * - userId, username, role을 claim에 포함
     */
    public String generateToken(UserDetails userDetails) {
        Date now = new Date();

        Long userId = null;
        if (userDetails instanceof CustomUserDetails) {
            userId = ((CustomUserDetails) userDetails).getUser().getId(); // 사용자 ID 추출
        }

        return Jwts.builder()
                .claim("userId", userId)
                .claim("username", userDetails.getUsername())
                .claim("role", getRoles(userDetails)) // 권한 목록
                .subject(userDetails.getUsername())
                .id(String.valueOf(userDetails.hashCode()))
                .issuedAt(now)
                .expiration(new Date(now.getTime() + this.expirationTime)) // 만료 시간
                .issuer(this.issuer)
                .signWith(this.hmacKey, Jwts.SIG.HS256) // 최신 서명 방식
                .compact();
    }

    /**
     * 권한(ROLE_*) 목록을 문자열 리스트로 추출하여 Claim에 넣기
     */
    private List<String> getRoles(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

    /**
     * JWT 파싱하여 Claims 반환
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(this.hmacKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 토큰 만료 여부 확인
     */
    private boolean isTokenExpired(String token) {
        return getExpirationDateFromToken(token).before(new Date());
    }

    /**
     * 만료 시간 추출
     */
    private Date getExpirationDateFromToken(String token) {
        return getAllClaimsFromToken(token).getExpiration();
    }

    /**
     * 토큰에서 사용자 식별자(subject, email)를 추출
     */
    public String getSubjectFromToken(String token) {
        return getAllClaimsFromToken(token).getSubject();
    }

    /**
     * 토큰 유효성 검증
     * - 만료 여부
     * - 사용자 정보 일치 여부
     */
    public boolean validateToken(String token, User user) {
        if (isTokenExpired(token)) return false;

        String subject = getSubjectFromToken(token);
        return subject != null && subject.equals(user.getEmail());
    }
}
