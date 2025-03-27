package com.ringle.domain.availability.service;

import com.ringle.common.exception.BusinessException;
import com.ringle.common.exception.ExceptionCode;
import com.ringle.domain.availability.dto.response.TimeSlotDto;
import com.ringle.domain.availability.dto.response.TutorSlotDto;
import com.ringle.domain.availability.entity.Availability;
import com.ringle.domain.availability.repository.AvailabilityRepository;
import com.ringle.domain.lesson.entity.enums.DurationType;
import com.ringle.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 학생이 수업 가능 시간대 및 튜터 목록을 조회하는 서비스
 */
@Service
@RequiredArgsConstructor
public class StudentAvailabilityService {

    private final AvailabilityRepository availabilityRepository;

    /**
     * 날짜 & 수업 길이 기반 수업 가능 시간대 조회
     * - 오늘: 현재 시간 이후의 정각 또는 30분 단위부터 시작
     * - 미래 날짜: 00:00부터 전체 시간대 확인
     * - 과거: 예외 발생
     *
     * @param targetDate 조회할 날짜
     * @param durationType 수업 길이 (30분/60분)
     * @return 수업 가능(true)한 시간대만 포함된 리스트
     */
    public List<TimeSlotDto> getAvailableTimeSlots(LocalDate targetDate, DurationType durationType) {
        // 입력 값 검증
        if (targetDate == null || durationType == null) {
            throw new BusinessException(ExceptionCode.NOT_VALID_ERROR);
        }

        // 오늘 날짜 및 현재 시각(초, 나노초 제거)
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);

        // 과거 날짜일 경우 예외 발생
        if (targetDate.isBefore(today)) {
            throw new BusinessException(ExceptionCode.DATE_IN_THE_PAST);
        }

        LocalDateTime startTime;

        // 오늘 날짜인 경우: 현재 시간 기준으로 30분 단위 정렬
        if (targetDate.isEqual(today)) {
            int minute = now.getMinute();                            // 현재 분 추출
            int roundedMinute = (minute < 30) ? 30 : 60;             // 다음 30분 단위로 올림
            startTime = now.withMinute(0).plusMinutes(roundedMinute); // 정각/30분 단위로 맞추기
            if (startTime.getMinute() == 60) {
                startTime = startTime.plusHours(1).withMinute(0);    // 60분이면 다음 시간의 0분으로 설정
            }
            System.out.println(startTime);
        } else {
            // 미래 날짜인 경우: 00:00부터 시작
            startTime = targetDate.atStartOfDay();
        }

        // 조회 종료 시각은 23:59
        LocalDateTime endTime = targetDate.atTime(23, 59);
        List<TimeSlotDto> result = new ArrayList<>();

        // 30분 단위로 반복
        while (startTime.isBefore(endTime)) {
            boolean available = false;

            if (durationType == DurationType.THIRTY) {
                // 30분 수업의 경우 해당 시간에 예약 가능 여부 조회
                available = availabilityRepository.existsByStartTimeAndIsBookedFalse(startTime);
            } else if (durationType == DurationType.SIXTY) {
                // 60분 수업의 경우 연속된 두 슬롯이 모두 비어 있어야 함
                LocalDateTime nextSlot = startTime.plusMinutes(30);
                available =
                        availabilityRepository.existsByStartTimeAndIsBookedFalse(startTime) &&
                                availabilityRepository.existsByStartTimeAndIsBookedFalse(nextSlot);
            }

            // 신청 가능한 slot만 추가
            if (available) {
                result.add(TimeSlotDto.builder()
                        .time(startTime.toLocalTime().toString().substring(0, 5)) // HH:mm format
                        .available(true)
                        .build());
            }

            // 다음 시간 슬롯으로 이동
            startTime = startTime.plusMinutes(30);
        }

        return result;
    }


    /**
     * 특정 날짜에 수업 가능한 튜터 목록과 가능한 시간대 조회
     *
     * @param date 조회할 날짜
     * @return 튜터별 가능한 시간 리스트
     */
    @Transactional(readOnly = true)
    public List<TutorSlotDto> getTutorAvailableSlotsByDate(LocalDate date) {
        // 날짜 검증
        if (date == null) {
            throw new BusinessException(ExceptionCode.NOT_VALID_ERROR);
        }

        // 과거 날짜는 예약 불가
        if (date.isBefore(LocalDate.now())) {
            throw new BusinessException(ExceptionCode.DATE_IN_THE_PAST);
        }

        // 00:00 ~ 23:59 범위 설정
        LocalDateTime start = date.atTime(0, 0);
        LocalDateTime end = date.atTime(23, 59);

        List<Availability> availabilities = availabilityRepository.findUnbookedSlotsForToday(start, end);

        // 수업 가능한 튜터가 없는 경우 예외 처리
        if (availabilities.isEmpty()) {
            throw new BusinessException(ExceptionCode.TUTOR_AVAILABILITY_NOT_FOUND);
        }

        // 튜터별 시간대 정리 (LinkedHashMap → 순서 보장)
        Map<User, List<String>> tutorTimeMap = new LinkedHashMap<>();
        for (Availability a : availabilities) {
            tutorTimeMap
                    .computeIfAbsent(a.getTutor(), k -> new ArrayList<>())
                    .add(a.getStartTime().toLocalTime().toString().substring(0, 5)); // HH:mm
        }

        // 튜터별로 시간대 목록 DTO로 변환하여 반환
        return tutorTimeMap.entrySet().stream()
                .map(entry -> TutorSlotDto.builder()
                        .tutorId(entry.getKey().getId())
                        .tutorName(entry.getKey().getName())
                        .availableTimes(entry.getValue())
                        .build())
                .toList();
    }
}
