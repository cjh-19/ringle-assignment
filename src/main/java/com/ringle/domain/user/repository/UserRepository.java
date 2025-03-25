package com.ringle.domain.user.repository;

import com.ringle.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 사용자 엔티티용 JPA Repository
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email); // 이메일(email) 기반 사용자 조회
    boolean existsByEmail(String email); // 중복 이메일 검사
}
