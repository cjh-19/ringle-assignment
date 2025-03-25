package com.ringle.security;

import com.ringle.common.jwt.JwtUtils;
import com.ringle.domain.user.entity.User;
import com.ringle.domain.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 인증 필터
 * - 요청 헤더에서 토큰을 추출하고 유효성 검사
 * - 인증 성공 시 SecurityContext에 인증 정보 저장
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final CustomUserDetailsService userDetailsService;

    // 인증 없이 접근 가능한 URI 목록
    private static final List<String> EXCLUDE_PATHS = List.of(
            "/swagger-ui", "/v3/api-docs", "/error", "/api/auth"
    );

    /**
     * 토큰 검사 제외 경로 설정
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return EXCLUDE_PATHS.stream().anyMatch(uri::startsWith);
    }

    /**
     * JWT 필터 로직
     * - Authorization 헤더 → Bearer 토큰 추출 → 유효성 검사 → 인증 정보 설정
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // Bearer 토큰이 아닌 경우는 다음 필터로 넘김
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        String email = jwtUtils.getSubjectFromToken(token); // subject = email

        // 사용자 조회 및 유효성 검증
        User user = userRepository.findByEmail(email);
        if (user == null || !jwtUtils.validateToken(token, user)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid JWT Token");
            return;
        }

        // 인증 정보 SecurityContext에 등록
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
