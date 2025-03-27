package com.ringle.domain.availability.repository;

import com.ringle.domain.availability.entity.Availability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    boolean existsByStartTimeAndIsBookedFalse(LocalDateTime startTime); // 특정 시간에 예약되지 않은 수업 가능 시간 존재 여부 확인

    // 특정 날짜 이후 가능한 tutor 전체 조회 (예약 안 된 것만)
    @Query("SELECT a FROM Availability a " +
            "WHERE a.isBooked = false AND a.startTime BETWEEN :start AND :end " +
            "ORDER BY a.startTime ASC")
    List<Availability> findUnbookedSlotsForToday(@Param("start") LocalDateTime start,
                                                 @Param("end") LocalDateTime end);
}
