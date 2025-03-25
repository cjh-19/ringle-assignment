package com.ringle.domain.availability.repository;

import com.ringle.domain.availability.entity.Availability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 수업 가능 시간 JPA Repository
 */
@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, Long> {
    List<Availability> findByTutorIdOrderByStartTimeAsc(Long tutorId); // 튜터가 등록한 수업 시간 목록을 조회
    boolean existsByTutorIdAndStartTime(Long tutorId, LocalDateTime startTime); // 해당 시간대에 등록한 수업이 있는지 조회
}
