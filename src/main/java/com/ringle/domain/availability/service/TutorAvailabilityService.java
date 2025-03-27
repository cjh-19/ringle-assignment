package com.ringle.domain.availability.service;

import com.ringle.common.exception.BusinessException;
import com.ringle.common.exception.ExceptionCode;
import com.ringle.domain.availability.dto.request.AvailabilityRequestDto;
import com.ringle.domain.availability.dto.response.AvailabilityResponseDto;
import com.ringle.domain.availability.entity.Availability;
import com.ringle.domain.availability.repository.AvailabilityRepository;
import com.ringle.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 수업 가능 시간 관련 비즈니스 로직
 */
@Service
@RequiredArgsConstructor
public class TutorAvailabilityService {

    private final AvailabilityRepository availabilityRepository;

    /**
     * 수업 가능 시간 등록
     * - 정각 또는 30분 시작만 허용
     * - 수업 길이 기준으로 종료 시간 자동 계산
     */
    @Transactional
    public void createAvailability(AvailabilityRequestDto request, User tutor) {
        LocalDateTime start = request.getStartTime();
        int minute = start.getMinute();

        // 현재 이전의 시간 등록 시 예외
        if (start.isBefore(LocalDateTime.now())) {
            throw new BusinessException(ExceptionCode.AVAILABILITY_TIME_PASSED);
        }

        // 정각 또는 30분 단위가 아닐 경우 예외
        if (minute != 0 && minute != 30) {
            throw new BusinessException(ExceptionCode.INVALID_START_TIME);
        }

        // 30분 단위로 시간 나눠서 저장
        int duration = request.getDuration().getMinutes(); // 30 또는 60
        int slots = duration / 30;

        for (int i = 0; i < slots; i++) {
            LocalDateTime slotStart = start.plusMinutes(30L * i);
            LocalDateTime slotEnd = slotStart.plusMinutes(30);

            // 이미 해당 시간대에 등록된 것이 있다면 skip
            boolean exists = availabilityRepository.existsByTutorIdAndStartTime(tutor.getId(), slotStart);
            if (exists) continue;

            Availability availability = Availability.builder()
                    .tutor(tutor)
                    .startTime(slotStart)
                    .endTime(slotEnd)
                    .isBooked(false)
                    .build();

            availabilityRepository.save(availability);
        }
    }

    /**
     * 수업 가능 시간 삭제
     * - 예약된 시간은 삭제 불가
     * - 자신의 것만 삭제 가능
     */
    @Transactional
    public void deleteAvailability(Long id, User tutor) {
        // 수업 조회 실패 예외
        Availability availability = availabilityRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ExceptionCode.AVAILABILITY_NOT_FOUND));

        // 다른 튜터의 수업 삭제 실패 예외
        if (!availability.getTutor().getId().equals(tutor.getId())) {
            throw new BusinessException(ExceptionCode.AVAILABILITY_UNAUTHORIZED);
        }

        // 이미 예약된 수업 예외
        if (availability.isBooked()) {
            throw new BusinessException(ExceptionCode.ALREADY_BOOKED);
        }

        availabilityRepository.delete(availability);
    }

    /**
     * 튜터가 등록한 수업 가능 시간 전체 조회
     */
    @Transactional(readOnly = true)
    public List<AvailabilityResponseDto> getMyAvailabilities(User tutor) {
        return availabilityRepository.findByTutorIdOrderByStartTimeAsc(tutor.getId())
                .stream()
                .map(a -> AvailabilityResponseDto.builder()
                        .id(a.getId())
                        .startTime(a.getStartTime())
                        .endTime(a.getEndTime())
                        .isBooked(a.isBooked())
                        .build())
                .toList();
    }
}
