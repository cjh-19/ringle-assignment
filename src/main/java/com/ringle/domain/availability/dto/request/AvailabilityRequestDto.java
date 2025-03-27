package com.ringle.domain.availability.dto.request;

import com.ringle.domain.lesson.entity.enums.DurationType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 수업 가능 시간 등록 요청 DTO
 */
@Getter
public class AvailabilityRequestDto {

    @NotNull(message = "시작 시간은 필수입니다.")
    private LocalDateTime startTime;

    @NotNull(message = "수업 길이는 필수입니다.")
    private DurationType duration;  // THIRTY 또는 SIXTY
}