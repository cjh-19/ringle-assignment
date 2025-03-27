package com.ringle.domain.lesson.service;

import com.ringle.common.exception.BusinessException;
import com.ringle.common.exception.ExceptionCode;
import com.ringle.common.lock.RedisLockManager;
import com.ringle.domain.availability.entity.Availability;
import com.ringle.domain.availability.repository.AvailabilityRepository;
import com.ringle.domain.lesson.dto.request.LessonRequestDto;
import com.ringle.domain.lesson.dto.response.LessonInfoResponseDto;
import com.ringle.domain.lesson.entity.Lesson;
import com.ringle.domain.lesson.entity.enums.DurationType;
import com.ringle.domain.lesson.entity.enums.LessonStatus;
import com.ringle.domain.lesson.repository.LessonRepository;
import com.ringle.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 수업 관련 비즈니스 로직
 */
@Service
@RequiredArgsConstructor
public class LessonService {

    private final AvailabilityRepository availabilityRepository;
    private final LessonRepository lessonRepository;
    private final RedisLockManager redisLockManager;

    /**
     * 학생이 신청한 수업 전체 조회
     * - student 현재 로그인한 사용자 (학생)에 대해서
     * - LessonSummaryResponseDto 리스트 반환 (없을 경우 빈 리스트)
     */
    @Transactional(readOnly = true)
    public List<LessonInfoResponseDto> getLessonsByStudent(User student) {
        // 해당 학생이 신청한 수업을 시작 시간 기준으로 역순 정렬해서 조회
        List<Lesson> lessons = lessonRepository.findByStudentIdOrderByStartTimeDesc(student.getId());

        // 각 Lesson을 응답용 DTO로 변환
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

    /**
     * 수업 신청 서비스
     * - Redis 기반 분산 락으로 동시성 문제 해결
     * - 대체 튜터 매칭 로직 포함
     */
    public void bookLesson(LessonRequestDto request, User student) {
        // Redis 분산 락 키: tutorId + startTime 조합
        String lockKey = "lesson:" + request.getTutorId() + ":" + request.getStartTime();

        redisLockManager.runWithLock(lockKey, 3, 5, () -> {
            DurationType type = request.getDurationType();
            LocalDateTime start = request.getStartTime();
            LocalDateTime end = start.plusMinutes(type.getMinutes());

            // 1. 지정한 튜터의 예약 가능 시간대 조회
            List<Availability> slots = availabilityRepository.findAvailableSlots(
                    request.getTutorId(), start, end
            );

            // 2. 수업 길이에 따라 슬롯 유효성 검사
            boolean available = isValidSlot(slots, start, type);

            // 3. 예약 불가능한 경우
            if (!available) {
                // 대체 튜터 허용 시
                if (request.isAllowAlternativeTutor()) {
                    // 다른 튜터 중 같은 시간대 예약 가능 슬롯 탐색
                    List<Availability> alternatives = availabilityRepository.findAlternativeSlots(
                            request.getTutorId(), start, end
                    );

                    // 60분 수업이라면 연속된 2개 슬롯 필수
                    List<Availability> grouped = getValidAlternativeGroup(alternatives, type, start);

                    if (!grouped.isEmpty()) {
                        // 대체 튜터 수업 생성 + 예약 처리
                        createLesson(student, grouped.get(0).getTutor(), start, end, type);
                        grouped.forEach(a -> {
                            a.setBooked(true);
                            availabilityRepository.save(a);
                        });
                        return null;
                    }
                }

                // 대체 튜터 불가 혹은 없음 → 예외
                throw new BusinessException(ExceptionCode.NO_AVAILABLE_TUTOR);
            }

            // 4. 정상 예약 가능 → 수업 생성
            createLesson(student, slots.get(0).getTutor(), start, end, type);

            // 5. 예약된 시간대에 대해 booked 처리
            slots.forEach(a -> {
                a.setBooked(true);
                availabilityRepository.save(a);
            });

            return null;
        });
    }

    /**
     * 해당 수업 길이(DurationType)에 따라 예약 가능한 시간대인지 확인
     */
    private boolean isValidSlot(List<Availability> slots, LocalDateTime start, DurationType type) {
        if (type == DurationType.THIRTY) {
            // 30분 수업이면 해당 시작 시간에 슬롯이 하나 있어야 함
            return slots.size() == 1 && slots.get(0).getStartTime().equals(start);
        } else {
            // 60분 수업이면 시작 시각 + 30분 슬롯 모두 존재해야 함
            return slots.size() >= 2 &&
                    slots.stream().anyMatch(a -> a.getStartTime().equals(start)) &&
                    slots.stream().anyMatch(a -> a.getStartTime().equals(start.plusMinutes(30)));
        }
    }

    /**
     * 대체 튜터 후보 중에서 유효한 연속 슬롯(30분x2) 조합 반환
     */
    private List<Availability> getValidAlternativeGroup(List<Availability> list, DurationType type, LocalDateTime start) {
        if (type == DurationType.THIRTY) {
            // 30분 수업은 해당 시간 슬롯 하나만 있으면 됨
            return list.stream()
                    .filter(a -> a.getStartTime().equals(start))
                    .findFirst()
                    .map(List::of)
                    .orElse(List.of());
        } else {
            // 60분 수업은 튜터별로 그룹핑하여 연속된 슬롯 2개가 있는 튜터를 찾음
            return list.stream()
                    .collect(Collectors.groupingBy(a -> a.getTutor().getId()))
                    .values().stream()
                    .filter(slots -> slots.size() >= 2)
                    .map(slots -> {
                        Map<LocalDateTime, Availability> slotMap = slots.stream()
                                .collect(Collectors.toMap(Availability::getStartTime, a -> a));
                        Availability first = slotMap.get(start);
                        Availability second = slotMap.get(start.plusMinutes(30));
                        if (first != null && second != null) {
                            return List.of(first, second);
                        }
                        return null;
                    })
                    .filter(group -> group != null)
                    .findFirst()
                    .orElse(List.of());
        }
    }

    /**
     * 수업 엔티티 생성 및 저장
     */
    private void createLesson(User student, User tutor, LocalDateTime start, LocalDateTime end, DurationType type) {
        Lesson lesson = Lesson.builder()
                .student(student)
                .tutor(tutor)
                .startTime(start)
                .endTime(end)
                .durationType(type)
                .status(LessonStatus.CONFIRMED)
                .build();

        lessonRepository.save(lesson);
    }
}