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

    /**
     * 지정한 튜터의 수업 가능 시간 목록 조회
     * - 시작 시각 이상, 종료 시각 이하 범위에서 예약되지 않은 시간대만 반환
     */
    @Query("SELECT a FROM Availability a " +
            "WHERE a.tutor.id = :tutorId AND a.startTime >= :start AND a.endTime <= :end AND a.isBooked = false")
    List<Availability> findAvailableSlots(Long tutorId, LocalDateTime start, LocalDateTime end);

    /**
     * 대체 튜터 후보 검색
     * - 현재 튜터를 제외한 다른 튜터 중 같은 시간대에 예약되지 않은 시간대 보유한 튜터 목록
     */
    @Query("SELECT a FROM Availability a " +
            "WHERE a.tutor.id <> :excludedTutorId AND a.startTime >= :start AND a.endTime <= :end " +
            "AND a.isBooked = false AND a.tutor.role = com.ringle.domain.user.entity.enums.Role.TUTOR " +
            "ORDER BY a.startTime ASC")
    List<Availability> findAlternativeSlots(Long excludedTutorId, LocalDateTime start, LocalDateTime end);
}
