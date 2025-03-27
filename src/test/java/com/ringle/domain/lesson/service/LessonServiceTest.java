package com.ringle.domain.lesson.service;

import com.ringle.common.exception.BusinessException;
import com.ringle.common.exception.ExceptionCode;
import com.ringle.common.lock.RedisLockManager;
import com.ringle.domain.availability.entity.Availability;
import com.ringle.domain.availability.repository.AvailabilityRepository;
import com.ringle.domain.lesson.dto.request.LessonRequestDto;
import com.ringle.domain.lesson.entity.Lesson;
import com.ringle.domain.lesson.entity.enums.DurationType;
import com.ringle.domain.lesson.repository.LessonRepository;
import com.ringle.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * LessonService의 bookLesson() 메서드에 대한 단위 테스트 클래스
 * - RedisLockManager를 통해 동시성 테스트도 포함
 * - 다양한 시나리오(정상 예약, 대체 튜터, 예약 불가)를 검증함
 */
class LessonServiceTest {

    @InjectMocks
    private LessonService lessonService;

    @Mock
    private AvailabilityRepository availabilityRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private RedisLockManager redisLockManager;

    @BeforeEach
    void setup() {
        // @Mock 애노테이션 초기화
        MockitoAnnotations.openMocks(this);
    }

    /**
     * [정상 예약 테스트]
     * - 주어진 tutor, startTime 기준 예약 가능한 슬롯이 있을 때 수업을 생성하고 저장하는 시나리오
     */
    @Test
    void testBookLesson_Success() {
        // given: 테스트 입력값과 Mock 객체 설정
        LessonRequestDto request = new LessonRequestDto();
        ReflectionTestUtils.setField(request, "tutorId", 1L);
        ReflectionTestUtils.setField(request, "startTime", LocalDateTime.of(2025, 3, 28, 14, 0));
        ReflectionTestUtils.setField(request, "durationType", DurationType.THIRTY);
        ReflectionTestUtils.setField(request, "allowAlternativeTutor", false);

        User student = User.builder().id(100L).build();
        User tutor = User.builder().id(1L).build();

        Availability slot = Availability.builder()
                .tutor(tutor)
                .startTime(request.getStartTime())
                .endTime(request.getStartTime().plusMinutes(30))
                .isBooked(false)
                .build();

        // Mock 반환값 설정: 해당 tutor의 예약 가능한 슬롯 1개 존재
        when(availabilityRepository.findAvailableSlots(anyLong(), any(), any()))
                .thenReturn(List.of(slot));

        // when: 분산 락 내부 실행 로직을 바로 실행되도록 세팅
        doAnswer(invocation -> {
            RedisLockManager.LockExecutor<?> executor = invocation.getArgument(3);
            executor.execute();
            return null;
        }).when(redisLockManager).runWithLock(anyString(), anyInt(), anyInt(), any());

        // then: 예외 없이 실행되며 수업이 저장되는지 확인
        assertDoesNotThrow(() -> lessonService.bookLesson(request, student));
        verify(lessonRepository, times(1)).save(any(Lesson.class));
    }

    /**
     * [대체 튜터가 매칭되는 시나리오]
     * - 원래 튜터의 슬롯이 없고 대체 가능한 튜터가 존재하여 수업이 성공적으로 예약되는 경우
     */
    @Test
    void testBookLesson_AlternativeTutorAssigned() {
        // given
        LocalDateTime startTime = LocalDateTime.of(2025, 3, 28, 15, 0);
        LessonRequestDto request = new LessonRequestDto();
        ReflectionTestUtils.setField(request, "tutorId", 1L);
        ReflectionTestUtils.setField(request, "startTime", startTime);
        ReflectionTestUtils.setField(request, "durationType", DurationType.SIXTY);
        ReflectionTestUtils.setField(request, "allowAlternativeTutor", true);

        User student = User.builder().id(200L).build();
        User altTutor = User.builder().id(99L).build();

        Availability alt1 = Availability.builder().tutor(altTutor).startTime(startTime).endTime(startTime.plusMinutes(30)).build();
        Availability alt2 = Availability.builder().tutor(altTutor).startTime(startTime.plusMinutes(30)).endTime(startTime.plusMinutes(60)).build();

        // 원래 튜터는 예약 불가, 대체 튜터 슬롯 2개 제공
        when(availabilityRepository.findAvailableSlots(anyLong(), any(), any()))
                .thenReturn(List.of());
        when(availabilityRepository.findAlternativeSlots(anyLong(), any(), any()))
                .thenReturn(List.of(alt1, alt2));

        doAnswer(invocation -> {
            RedisLockManager.LockExecutor<?> executor = invocation.getArgument(3);
            executor.execute();
            return null;
        }).when(redisLockManager).runWithLock(anyString(), anyInt(), anyInt(), any());

        // then
        assertDoesNotThrow(() -> lessonService.bookLesson(request, student));
        verify(lessonRepository, times(1)).save(any(Lesson.class));
    }

    /**
     * [수업 예약 실패 테스트]
     * - 대체 튜터도 없고 예약 가능한 슬롯도 없는 경우 예외가 발생해야 함
     */
    @Test
    void testBookLesson_NoTutorAvailable() {
        // given
        LessonRequestDto request = new LessonRequestDto();
        ReflectionTestUtils.setField(request, "tutorId", 1L);
        ReflectionTestUtils.setField(request, "startTime", LocalDateTime.of(2025, 3, 28, 16, 0));
        ReflectionTestUtils.setField(request, "durationType", DurationType.THIRTY);
        ReflectionTestUtils.setField(request, "allowAlternativeTutor", false);

        User student = User.builder().id(300L).build();

        // 메인 튜터도 대체 튜터도 없는 경우
        when(availabilityRepository.findAvailableSlots(anyLong(), any(), any()))
                .thenReturn(List.of());

        doAnswer(invocation -> {
            RedisLockManager.LockExecutor<?> executor = invocation.getArgument(3);
            return executor.execute();
        }).when(redisLockManager).runWithLock(anyString(), anyInt(), anyInt(), any());

        // then: 예외 발생 검증
        BusinessException ex = assertThrows(BusinessException.class, () ->
                lessonService.bookLesson(request, student));

        assertEquals(ExceptionCode.NO_AVAILABLE_TUTOR, ex.getCode());
    }

    /**
     * [Redis 분산 락 실행 여부 검증]
     * - runWithLock() 메서드가 정확히 한 번 호출되었는지 확인
     */
    @Test
    void testDistributedLock_IsUsed() {
        // given
        LessonRequestDto request = new LessonRequestDto();
        ReflectionTestUtils.setField(request, "tutorId", 1L);
        ReflectionTestUtils.setField(request, "startTime", LocalDateTime.of(2025, 3, 28, 9, 0));
        ReflectionTestUtils.setField(request, "durationType", DurationType.THIRTY);
        ReflectionTestUtils.setField(request, "allowAlternativeTutor", false);

        when(availabilityRepository.findAvailableSlots(anyLong(), any(), any()))
                .thenReturn(List.of(
                        Availability.builder()
                                .tutor(User.builder().id(1L).build())
                                .startTime(LocalDateTime.of(2025, 3, 28, 9, 0))
                                .build()
                ));

        doAnswer(invocation -> {
            RedisLockManager.LockExecutor<?> executor = invocation.getArgument(3);
            executor.execute();
            return null;
        }).when(redisLockManager).runWithLock(anyString(), anyInt(), anyInt(), any());

        // when
        lessonService.bookLesson(request, User.builder().id(99L).build());

        // then
        verify(redisLockManager, times(1)).runWithLock(anyString(), anyInt(), anyInt(), any());
    }
}