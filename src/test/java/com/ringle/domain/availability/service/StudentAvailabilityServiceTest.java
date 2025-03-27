package com.ringle.domain.availability.service;

import com.ringle.common.exception.BusinessException;
import com.ringle.common.exception.ExceptionCode;
import com.ringle.domain.availability.dto.response.TimeSlotDto;
import com.ringle.domain.availability.repository.AvailabilityRepository;
import com.ringle.domain.lesson.entity.enums.DurationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class StudentAvailabilityServiceTest {

    @Mock
    private AvailabilityRepository availabilityRepository; // 의존성 Mock 선언

    @InjectMocks
    private StudentAvailabilityService studentAvailabilityService; // 테스트 대상 클래스에 Mock 주입

    @BeforeEach
    void setUp() {
        // Mockito 어노테이션 초기화
        MockitoAnnotations.openMocks(this);
    }

    /**
     * [정상 케이스]
     * - 미래 날짜에 30분 수업 요청이 들어왔고,
     * - 해당 시간대에 예약 가능한 슬롯이 존재하는 경우,
     * - 수업 가능 시간대 리스트가 반환되어야 함
     */
    @Test
    void getAvailableTimeSlots_thirtyMinutes_success() {
        LocalDate targetDate = LocalDate.now().plusDays(1); // 미래 날짜 설정
        LocalDateTime slotTime = targetDate.atTime(10, 0); // 10:00 시간대

        // Mock 설정: 해당 시간대는 예약 가능 상태
        when(availabilityRepository.existsByStartTimeAndIsBookedFalse(slotTime)).thenReturn(true);

        // when: 30분 수업 가능 시간 조회
        List<TimeSlotDto> result = studentAvailabilityService.getAvailableTimeSlots(targetDate, DurationType.THIRTY);

        // then: 결과 리스트가 비어있지 않고, 해당 시간대는 수업 가능해야 함
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).isAvailable()).isTrue();
    }

    /**
     * [정상 케이스]
     * - 미래 날짜에 60분 수업 요청이 들어왔고,
     * - 해당 시간대 + 다음 슬롯까지 모두 예약 가능할 경우,
     * - 해당 시간대가 수업 가능 시간으로 반환되어야 함
     */
    @Test
    void getAvailableTimeSlots_sixtyMinutes_success() {
        LocalDate targetDate = LocalDate.now().plusDays(1); // 미래 날짜
        LocalDateTime slot1 = targetDate.atTime(10, 0);     // 10:00
        LocalDateTime slot2 = slot1.plusMinutes(30);        // 10:30

        // Mock 설정: 두 슬롯 모두 예약 가능
        when(availabilityRepository.existsByStartTimeAndIsBookedFalse(slot1)).thenReturn(true);
        when(availabilityRepository.existsByStartTimeAndIsBookedFalse(slot2)).thenReturn(true);

        // when
        List<TimeSlotDto> result = studentAvailabilityService.getAvailableTimeSlots(targetDate, DurationType.SIXTY);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).isAvailable()).isTrue();
    }

    /**
     * [예외 케이스]
     * - 날짜 또는 수업 길이(DurationType)가 null인 경우
     * - 예외(NOT_VALID_ERROR) 발생
     */
    @Test
    void getAvailableTimeSlots_invalidInput_throwsException() {
        // 날짜가 null인 경우 예외 검증
        assertThatThrownBy(() -> studentAvailabilityService.getAvailableTimeSlots(null, DurationType.THIRTY))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ExceptionCode.NOT_VALID_ERROR.getMessage());

        // DurationType이 null인 경우 예외 검증
        assertThatThrownBy(() -> studentAvailabilityService.getAvailableTimeSlots(LocalDate.now(), null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ExceptionCode.NOT_VALID_ERROR.getMessage());
    }

    /**
     * [예외 케이스]
     * - 과거 날짜로 요청한 경우,
     * - 예외(DATE_IN_THE_PAST) 발생
     */
    @Test
    void getAvailableTimeSlots_pastDate_throwsException() {
        LocalDate yesterday = LocalDate.now().minusDays(1); // 어제 날짜

        // 과거 날짜로 수업 시간 조회 시 예외 검증
        assertThatThrownBy(() -> studentAvailabilityService.getAvailableTimeSlots(yesterday, DurationType.THIRTY))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ExceptionCode.DATE_IN_THE_PAST.getMessage());
    }

    /**
     * [예외 케이스]
     * - 튜터 수업 가능 시간 조회 시 날짜가 null인 경우
     * - 예외(NOT_VALID_ERROR) 발생
     */
    @Test
    void getTutorAvailableSlotsByDate_nullDate_throwsException() {
        assertThatThrownBy(() -> studentAvailabilityService.getTutorAvailableSlotsByDate(null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ExceptionCode.NOT_VALID_ERROR.getMessage());
    }

    /**
     * [예외 케이스]
     * - 과거 날짜로 튜터 수업 가능 시간 요청한 경우
     * - 예외(DATE_IN_THE_PAST) 발생
     */
    @Test
    void getTutorAvailableSlotsByDate_pastDate_throwsException() {
        LocalDate yesterday = LocalDate.now().minusDays(1); // 어제 날짜

        // 과거 날짜 요청 시 예외 검증
        assertThatThrownBy(() -> studentAvailabilityService.getTutorAvailableSlotsByDate(yesterday))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ExceptionCode.DATE_IN_THE_PAST.getMessage());
    }

    /**
     * [예외 케이스]
     * - 주어진 날짜에 예약 가능한 튜터가 한 명도 없을 경우
     * - 예외(TUTOR_AVAILABILITY_NOT_FOUND) 발생
     */
    @Test
    void getTutorAvailableSlotsByDate_emptySlots_throwsException() {
        LocalDate date = LocalDate.now().plusDays(1); // 미래 날짜

        // Mock 설정: 예약 가능한 튜터 없음
        when(availabilityRepository.findUnbookedSlotsForToday(any(), any()))
                .thenReturn(List.of()); // 빈 리스트 반환

        // 예외 검증
        assertThatThrownBy(() -> studentAvailabilityService.getTutorAvailableSlotsByDate(date))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ExceptionCode.TUTOR_AVAILABILITY_NOT_FOUND.getMessage());
    }
}
