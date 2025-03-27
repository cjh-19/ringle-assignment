package com.ringle.common.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redis 설정 클래스
 * - Redisson 클라이언트를 빈으로 등록
 * - Redis 기반의 분산 락을 사용할 수 있도록 설정
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        // 단일 Redis 서버 주소 지정
        config.useSingleServer().setAddress("redis://localhost:6379");
        return Redisson.create(config);
    }
}
