package com.ringle.domain.lesson.service;

import com.ringle.domain.lesson.dto.LessonInfoResponseDto;
import com.ringle.domain.lesson.entity.Lesson;
import com.ringle.domain.lesson.repository.LessonRepository;
import com.ringle.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 수업 관련 비즈니스 로직
 */
@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;

    /**
     * 학생이 신청한 수업 전체 조회
     * - student 현재 로그인한 사용자 (학생)에 대해서
     * - LessonSummaryResponseDto 리스트 반환 (없을 경우 빈 리스트)
     */
    @Transactional(readOnly = true)
    public List<LessonInfoResponseDto> getLessonsByStudent(User student) {
        List<Lesson> lessons = lessonRepository.findByStudentIdOrderByStartTimeDesc(student.getId());

        return lessons
                .stream()
                .map(lesson -> LessonInfoResponseDto.builder()
                        .lessonId(lesson.getId())
                        .startTime(lesson.getStartTime())
                        .endTime(lesson.getEndTime())
                        .durationType(lesson.getDurationType())
                        .status(lesson.getStatus())
                        .tutorName(lesson.getTutor().getName())
                        .tutorEmail(lesson.getTutor().getEmail())
                        .build())
                .toList();
    }
}
