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
    private AvailabilityRepository availabilityRepository;

    @InjectMocks
    private StudentAvailabilityService studentAvailabilityService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * [정상 케이스] 미래 날짜의 30분 수업 가능 시간대 조회
     */
    @Test
    void getAvailableTimeSlots_thirtyMinutes_success() {
        LocalDate targetDate = LocalDate.now().plusDays(1); // 미래 날짜
        LocalDateTime slotTime = targetDate.atTime(10, 0);

        // Mock: 해당 시간대 예약 가능
        when(availabilityRepository.existsByStartTimeAndIsBookedFalse(slotTime)).thenReturn(true);

        List<TimeSlotDto> result = studentAvailabilityService.getAvailableTimeSlots(targetDate, DurationType.THIRTY);

        // 검증: 하나 이상의 시간대 포함
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).isAvailable()).isTrue();
    }

    /**
     * [정상 케이스] 미래 날짜의 60분 수업 가능 시간대 조회
     */
    @Test
    void getAvailableTimeSlots_sixtyMinutes_success() {
        LocalDate targetDate = LocalDate.now().plusDays(1);
        LocalDateTime slot1 = targetDate.atTime(10, 0);
        LocalDateTime slot2 = slot1.plusMinutes(30);

        // Mock: 두 슬롯 모두 예약 가능
        when(availabilityRepository.existsByStartTimeAndIsBookedFalse(slot1)).thenReturn(true);
        when(availabilityRepository.existsByStartTimeAndIsBookedFalse(slot2)).thenReturn(true);

        List<TimeSlotDto> result = studentAvailabilityService.getAvailableTimeSlots(targetDate, DurationType.SIXTY);

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).isAvailable()).isTrue();
    }

    /**
     * [예외 케이스] 날짜 또는 DurationType이 null인 경우
     */
    @Test
    void getAvailableTimeSlots_invalidInput_throwsException() {
        assertThatThrownBy(() -> studentAvailabilityService.getAvailableTimeSlots(null, DurationType.THIRTY))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ExceptionCode.NOT_VALID_ERROR.getMessage());

        assertThatThrownBy(() -> studentAvailabilityService.getAvailableTimeSlots(LocalDate.now(), null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ExceptionCode.NOT_VALID_ERROR.getMessage());
    }

    /**
     * [예외 케이스] 과거 날짜 요청 시 예외 발생
     */
    @Test
    void getAvailableTimeSlots_pastDate_throwsException() {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        assertThatThrownBy(() -> studentAvailabilityService.getAvailableTimeSlots(yesterday, DurationType.THIRTY))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ExceptionCode.DATE_IN_THE_PAST.getMessage());
    }

    /**
     * [예외 케이스] 튜터 목록 조회 시 날짜가 null이면 예외
     */
    @Test
    void getTutorAvailableSlotsByDate_nullDate_throwsException() {
        assertThatThrownBy(() -> studentAvailabilityService.getTutorAvailableSlotsByDate(null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ExceptionCode.NOT_VALID_ERROR.getMessage());
    }

    /**
     * [예외 케이스] 과거 날짜에 대해 튜터 목록 조회 시 예외 발생
     */
    @Test
    void getTutorAvailableSlotsByDate_pastDate_throwsException() {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        assertThatThrownBy(() -> studentAvailabilityService.getTutorAvailableSlotsByDate(yesterday))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ExceptionCode.DATE_IN_THE_PAST.getMessage());
    }

    /**
     * [예외 케이스] 튜터 수업 가능 시간이 없는 경우
     */
    @Test
    void getTutorAvailableSlotsByDate_emptySlots_throwsException() {
        LocalDate date = LocalDate.now().plusDays(1);

        when(availabilityRepository.findUnbookedSlotsForToday(any(), any()))
                .thenReturn(List.of()); // 빈 리스트 반환

        assertThatThrownBy(() -> studentAvailabilityService.getTutorAvailableSlotsByDate(date))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ExceptionCode.TUTOR_AVAILABILITY_NOT_FOUND.getMessage());
    }
}
