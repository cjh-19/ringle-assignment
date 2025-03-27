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
        // 가짜 repository 생성 (Mockito)
        availabilityRepository = mock(AvailabilityRepository.class);
        // 서비스 객체 생성
        tutorAvailabilityService = new TutorAvailabilityService(availabilityRepository);

        // 테스트용 tutor 객체 생성
        tutor = User.builder()
                .id(1L)
                .name("Test Tutor")
                .build();
    }

    /**
     * [정상 등록 테스트]
     * - 60분 수업 등록 요청이 들어왔고,
     * - 정각 또는 30분 단위이며,
     * - 중복된 시간대가 없을 경우,
     * - 두 개의 Availability 슬롯이 저장되어야 함
     */
    @Test
    void createAvailability_정상등록_성공() {
        // given
        LocalDateTime startTime = LocalDateTime.now().plusDays(1).withMinute(0); // 내일 정각
        AvailabilityRequestDto request = new AvailabilityRequestDto();
        request.setStartTime(startTime);
        request.setDuration(DurationType.SIXTY); // 60분 요청 → 30분 슬롯 2개 필요

        // 두 슬롯 모두 존재하지 않는 것으로 설정
        when(availabilityRepository.existsByTutorIdAndStartTime(anyLong(), any())).thenReturn(false);

        // when
        tutorAvailabilityService.createAvailability(request, tutor);

        // then
        // 두 슬롯 저장되었는지 검증
        verify(availabilityRepository, times(2)).save(any(Availability.class));
    }

    /**
     * [예외 테스트] 시작 시간이 정각 또는 30분 단위가 아닌 경우
     * - 예외(INVALID_START_TIME) 발생해야 함
     */
    @Test
    void createAvailability_정각아닌시간_예외() {
        // given
        LocalDateTime invalidStart = LocalDateTime.now().plusDays(1).withMinute(15); // 15분 → 잘못된 시작 시간
        AvailabilityRequestDto request = new AvailabilityRequestDto();
        request.setStartTime(invalidStart);
        request.setDuration(DurationType.THIRTY);

        // then
        assertThatThrownBy(() -> tutorAvailabilityService.createAvailability(request, tutor))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ExceptionCode.INVALID_START_TIME.getMessage());
    }

    /**
     * [예외 테스트] 과거 시간대에 등록 요청한 경우
     * - 예외(AVAILABILITY_TIME_PASSED) 발생해야 함
     */
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

    /**
     * [정상 삭제 테스트]
     * - 본인의 예약되지 않은 슬롯을 삭제하는 경우
     */
    @Test
    void deleteAvailability_정상삭제_성공() {
        // given
        Availability availability = Availability.builder()
                .id(1L)
                .tutor(tutor)
                .startTime(LocalDateTime.now().plusDays(1))
                .isBooked(false)
                .build();

        // repository가 해당 슬롯을 반환한다고 설정
        when(availabilityRepository.findById(1L)).thenReturn(Optional.of(availability));

        // when
        tutorAvailabilityService.deleteAvailability(1L, tutor);

        // then
        verify(availabilityRepository).delete(availability); // 삭제 메서드가 호출되었는지 검증
    }

    /**
     * [예외 테스트] 다른 튜터의 슬롯을 삭제하려는 경우
     * - 예외(AVAILABILITY_UNAUTHORIZED) 발생해야 함
     */
    @Test
    void deleteAvailability_타튜터삭제시도_예외() {
        // given
        User anotherTutor = User.builder().id(99L).build(); // 현재 사용자가 아님

        Availability availability = Availability.builder()
                .id(1L)
                .tutor(anotherTutor)
                .startTime(LocalDateTime.now().plusDays(1))
                .isBooked(false)
                .build();

        // repository가 해당 availability 반환
        when(availabilityRepository.findById(1L)).thenReturn(Optional.of(availability));

        // then
        assertThatThrownBy(() -> tutorAvailabilityService.deleteAvailability(1L, tutor))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ExceptionCode.AVAILABILITY_UNAUTHORIZED.getMessage());
    }

    /**
     * [예외 테스트] 이미 예약된 슬롯을 삭제하려는 경우
     * - 예외(ALREADY_BOOKED) 발생해야 함
     */
    @Test
    void deleteAvailability_이미예약된경우_예외() {
        // given
        Availability availability = Availability.builder()
                .id(1L)
                .tutor(tutor)
                .startTime(LocalDateTime.now().plusDays(1))
                .isBooked(true) // 예약됨
                .build();

        // 해당 availability가 반환되도록 설정
        when(availabilityRepository.findById(1L)).thenReturn(Optional.of(availability));

        // then
        assertThatThrownBy(() -> tutorAvailabilityService.deleteAvailability(1L, tutor))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ExceptionCode.ALREADY_BOOKED.getMessage());
    }

    /**
     * [정상 조회 테스트]
     * - tutor의 수업 가능 시간들을 정상적으로 조회하고 DTO로 변환해야 함
     */
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

        // 가짜 availability 목록 반환
        when(availabilityRepository.findByTutorIdOrderByStartTimeAsc(tutor.getId()))
                .thenReturn(List.of(a1));

        // when
        List<AvailabilityResponseDto> result = tutorAvailabilityService.getMyAvailabilities(tutor);

        // then
        assertThat(result).hasSize(1); // 1개 조회되었는지 확인
        assertThat(result.get(0).getId()).isEqualTo(a1.getId()); // ID 일치 여부 검증
    }
}

