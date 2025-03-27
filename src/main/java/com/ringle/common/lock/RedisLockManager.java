package com.ringle.common.lock;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis 기반 분산 락 유틸리티
 * - 동시에 같은 리소스를 수정하지 않도록 락으로 보호
 * - 락 획득 시 작업 실행, 실패 시 예외 처리
 */
@Component
@RequiredArgsConstructor
public class RedisLockManager {

    private final RedissonClient redissonClient;

    public <T> T runWithLock(String key, int waitTime, int leaseTime, LockExecutor<T> executor) {
        RLock lock = redissonClient.getLock(key);
        try {
            // waitTime 안에 락 획득 시도, leaseTime 후 자동 해제
            if (lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS)) {
                return executor.execute();
            } else {
                throw new RuntimeException("Lock 획득 실패: 중복 요청 또는 처리 중입니다.");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Lock 처리 중 예외 발생", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock(); // 락 해제
            }
        }
    }

    @FunctionalInterface
    public interface LockExecutor<T> {
        T execute(); // 실제 실행할 작업 정의
    }
}
