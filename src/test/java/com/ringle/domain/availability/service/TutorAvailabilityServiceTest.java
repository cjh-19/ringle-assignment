package com.ringle.domain.availability.service;

import com.ringle.common.exception.BusinessException;
import com.ringle.common.exception.ExceptionCode;
import com.ringle.domain.availability.dto.request.AvailabilityRequestDto;
import com.ringle.domain.availability.dto.response.AvailabilityResponseDto;
import com.ringle.domain.availability.entity.Availability;
import com.ringle.domain.availability.repository.AvailabilityRepository;
import com.ringle.domain.lesson.entity.enums.DurationType;
import com.ringle.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class TutorAvailabilityServiceTest {

    private AvailabilityRepository availabilityRepository;
    private TutorAvailabilityService tutorAvailabilityService;

    private User tutor;

    @BeforeEach
    void setUp() {
        // 가짜(Mock) repository 생성
        availabilityRepository = mock(AvailabilityRepository.class);
        tutorAvailabilityService = new TutorAvailabilityService(availabilityRepository);

        // 튜터 테스트 객체 생성
        tutor = User.builder()
                .id(1L)
                .name("Test Tutor")
                .build();
    }

    @Test
    void createAvailability_정상등록_성공() {
        // given
        LocalDateTime startTime = LocalDateTime.now().plusDays(1).withMinute(0); // 내일 정각
        AvailabilityRequestDto request = new AvailabilityRequestDto();
        request.setStartTime(startTime);
        request.setDuration(DurationType.SIXTY); // 60분 수업 요청 → 30분 슬롯 2개 필요

        // 모든 슬롯이 존재하지 않는 상황 가정
        when(availabilityRepository.existsByTutorIdAndStartTime(anyLong(), any())).thenReturn(false);

        // when
        tutorAvailabilityService.createAvailability(request, tutor);

        // then
        // 30분 * 2개 = 2번 저장되어야 함
        verify(availabilityRepository, times(2)).save(any(Availability.class));
    }

    @Test
    void createAvailability_정각아닌시간_예외() {
        // given
        LocalDateTime invalidStart = LocalDateTime.now().plusDays(1).withMinute(15); // 15분 시작 (예외)
        AvailabilityRequestDto request = new AvailabilityRequestDto();
        request.setStartTime(invalidStart);
        request.setDuration(DurationType.THIRTY);

        // then
        assertThatThrownBy(() -> tutorAvailabilityService.createAvailability(request, tutor))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ExceptionCode.INVALID_START_TIME.getMessage());
    }

    @Test
    void createAvailability_과거시간등록_예외() {
        // given
        LocalDateTime pastTime = LocalDateTime.now().minusHours(1); // 과거 시간
        AvailabilityRequestDto request = new AvailabilityRequestDto();
        request.setStartTime(pastTime);
        request.setDuration(DurationType.THIRTY);

        // then
        assertThatThrownBy(() -> tutorAvailabilityService.createAvailability(request, tutor))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ExceptionCode.AVAILABILITY_TIME_PASSED.getMessage());
    }

    @Test
    void deleteAvailability_정상삭제_성공() {
        // given
        Availability availability = Availability.builder()
                .id(1L)
                .tutor(tutor)
                .startTime(LocalDateTime.now().plusDays(1))
                .isBooked(false)
                .build();

        // 튜터가 본인 슬롯을 삭제하는 상황
        when(availabilityRepository.findById(1L)).thenReturn(Optional.of(availability));

        // when
        tutorAvailabilityService.deleteAvailability(1L, tutor);

        // then
        verify(availabilityRepository).delete(availability);
    }

    @Test
    void deleteAvailability_타튜터삭제시도_예외() {
        // given
        User anotherTutor = User.builder().id(99L).build(); // 본인이 아님

        Availability availability = Availability.builder()
                .id(1L)
                .tutor(anotherTutor)
                .startTime(LocalDateTime.now().plusDays(1))
                .isBooked(false)
                .build();

        when(availabilityRepository.findById(1L)).thenReturn(Optional.of(availability));

        // then
        assertThatThrownBy(() -> tutorAvailabilityService.deleteAvailability(1L, tutor))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ExceptionCode.AVAILABILITY_UNAUTHORIZED.getMessage());
    }

    @Test
    void deleteAvailability_이미예약된경우_예외() {
        // given
        Availability availability = Availability.builder()
                .id(1L)
                .tutor(tutor)
                .startTime(LocalDateTime.now().plusDays(1))
                .isBooked(true) // 이미 예약됨
                .build();

        when(availabilityRepository.findById(1L)).thenReturn(Optional.of(availability));

        // then
        assertThatThrownBy(() -> tutorAvailabilityService.deleteAvailability(1L, tutor))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ExceptionCode.ALREADY_BOOKED.getMessage());
    }

    @Test
    void getMyAvailabilities_정상조회_성공() {
        // given
        Availability a1 = Availability.builder()
                .id(1L)
                .tutor(tutor)
                .startTime(LocalDateTime.now().plusHours(1))
                .endTime(LocalDateTime.now().plusHours(2))
                .isBooked(false)
                .build();

        // 가짜 응답 설정
        when(availabilityRepository.findByTutorIdOrderByStartTimeAsc(tutor.getId()))
                .thenReturn(List.of(a1));

        // when
        List<AvailabilityResponseDto> result = tutorAvailabilityService.getMyAvailabilities(tutor);

        // then
        assertThat(result).hasSize(1); // 결과 크기 확인
        assertThat(result.get(0).getId()).isEqualTo(a1.getId()); // ID 값 일치 확인
    }
}
