package com.ringle.common.config;

import com.ringle.security.CustomUserDetailsService;
import com.ringle.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@Slf4j
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService customUserDetailsService;

    private static final String[] SWAGGER_PATH = {
            "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/error"
    };

    private static final String[] PERMIT_ALL_PATH = {
            "/", "/api/auth/**", "/api/files/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable())

                .authorizeHttpRequests(auth -> auth
                        // 인증 없이 접근 가능한 경로
                        .requestMatchers(SWAGGER_PATH).permitAll()
                        .requestMatchers(PERMIT_ALL_PATH).permitAll()

                        // STUDENT 전용 API
                        .requestMatchers("/api/student/**").hasRole("STUDENT")

                        // TUTOR 전용 API
                        .requestMatchers("/api/tutor/**").hasRole("TUTOR")

                        // 공용 API (STUDENT, TUTOR 모두 가능)
                        .requestMatchers("/api/**").hasAnyRole("STUDENT", "TUTOR")

                        // 그 외 모든 요청 차단
                        .anyRequest().authenticated()
                )

                // JWT 기반 인증을 위해 세션 사용하지 않음
                .sessionManagement(sess -> sess
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        // JWT 필터 등록
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.userDetailsService(customUserDetailsService)
                .passwordEncoder(bCryptPasswordEncoder());
        return builder.build();
    }
}
