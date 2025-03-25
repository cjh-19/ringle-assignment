package com.ringle.domain.availability.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 수업 가능 시간 응답 DTO
 */
@Getter
@Builder
public class AvailabilityResponseDto {
    private Long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean isBooked;
}