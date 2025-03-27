package com.ringle.domain.lesson.dto.request;

import com.ringle.domain.lesson.entity.enums.DurationType;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 수업 신청 요청 DTO
 * - 수업 시간, 수업 길이, 튜터 ID, 대체 튜터 허용 여부 포함
 */
@Data
public class LessonRequestDto {
    private Long tutorId;
    private LocalDateTime startTime;
    private DurationType durationType;
    private boolean allowAlternativeTutor;
}
