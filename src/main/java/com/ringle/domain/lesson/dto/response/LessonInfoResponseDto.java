package com.ringle.domain.lesson.dto.response;

import com.ringle.domain.lesson.entity.enums.DurationType;
import com.ringle.domain.lesson.entity.enums.LessonStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 수업 목록 응답용 DTO
 * - 학생이 신청한 수업에 대해 튜터 정보 포함
 */
@Getter
@Builder
public class LessonInfoResponseDto {
    private Long lessonId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private DurationType durationType;
    private LessonStatus status;
    private String tutorName;
    private String tutorEmail;
}