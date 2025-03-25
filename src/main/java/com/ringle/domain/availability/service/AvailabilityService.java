package com.ringle.domain.availability.service;

import com.ringle.common.exception.BusinessException;
import com.ringle.common.exception.ExceptionCode;
import com.ringle.domain.availability.dto.request.AvailabilityRequestDto;
import com.ringle.domain.availability.entity.Availability;
import com.ringle.domain.availability.repository.AvailabilityRepository;
import com.ringle.domain.user.entity.User;
import com.ringle.common.exception.BusinessException;
import com.ringle.common.exception.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 수업 가능 시간 관련 비즈니스 로직
 */
@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final AvailabilityRepository availabilityRepository;

    /**
     * 수업 가능 시간 등록
     * - 정각 또는 30분 시작만 허용
     * - 수업 길이 기준으로 종료 시간 자동 계산
     */
    public void createAvailability(AvailabilityRequestDto request, User tutor) {
        LocalDateTime start = request.getStartTime();
        int minute = start.getMinute();

        // 정각 또는 30분 단위 검증
        if (minute != 0 && minute != 30) {
            throw new BusinessException(ExceptionCode.INVALID_START_TIME);
        }

        // 수업 길이로 종료 시간 계산
        LocalDateTime end = start.plusMinutes(request.getDuration().getMinutes());

        Availability availability = Availability.builder()
                .tutor(tutor)
                .startTime(start)
                .endTime(end)
                .isBooked(false)
                .build();

        availabilityRepository.save(availability);
    }

    /**
     * 수업 가능 시간 삭제
     * - 예약된 시간은 삭제 불가
     * - 자신의 것만 삭제 가능
     */
    public void deleteAvailability(Long id, User tutor) {
        Availability availability = availabilityRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ExceptionCode.AVAILABILITY_NOT_FOUND));

        if (!availability.getTutor().getId().equals(tutor.getId())) {
            throw new BusinessException(ExceptionCode.AVAILABILITY_UNAUTHORIZED);
        }

        if (availability.isBooked()) {
            throw new BusinessException(ExceptionCode.ALREADY_BOOKED);
        }

        availabilityRepository.delete(availability);
    }
}
