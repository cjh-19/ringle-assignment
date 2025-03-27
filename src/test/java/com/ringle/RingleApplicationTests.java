package com.ringle;

import com.ringle.common.exception.BusinessException;
import com.ringle.domain.availability.entity.Availability;
import com.ringle.domain.availability.repository.AvailabilityRepository;
import com.ringle.domain.lesson.dto.request.LessonRequestDto;
import com.ringle.domain.lesson.entity.Lesson;
import com.ringle.domain.lesson.entity.enums.DurationType;
import com.ringle.domain.lesson.repository.LessonRepository;
import com.ringle.domain.lesson.service.LessonService;
import com.ringle.domain.user.entity.User;
import com.ringle.domain.user.entity.enums.Role;
import com.ringle.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class RingleApplicationTests {

    @Autowired
    private LessonService lessonService;

    @Autowired
    private AvailabilityRepository availabilityRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedissonClient redissonClient;

    private User tutor;
    private User student;

    private LocalDateTime startTime;

    @BeforeEach
    void setup() {
        lessonRepository.deleteAll();
        availabilityRepository.deleteAll();
        userRepository.deleteAll();

        tutor = userRepository.save(User.builder()
                .name("Tutor")
                .email("tutor@example.com")
                .password("encoded")
                .role(Role.TUTOR)
                .build());

        student = userRepository.save(User.builder()
                .name("Student")
                .email("student@example.com")
                .password("encoded")
                .role(Role.STUDENT)
                .build());

        startTime = LocalDateTime.of(2025, 3, 28, 14, 0);

        // 수업 가능 시간 슬롯 2개 등록 (60분 수업용)
        availabilityRepository.save(Availability.builder()
                .tutor(tutor)
                .startTime(startTime)
                .endTime(startTime.plusMinutes(30))
                .isBooked(false)
                .build());

        availabilityRepository.save(Availability.builder()
                .tutor(tutor)
                .startTime(startTime.plusMinutes(30))
                .endTime(startTime.plusMinutes(60))
                .isBooked(false)
                .build());
    }

    /**
     * [동시성 테스트]
     * - 여러 명이 동시에 같은 시간대 수업을 신청할 경우
     * - 분산 락이 정상 동작하면 단 하나의 수업만 생성되어야 함
     */
    @Test
    void testConcurrentLessonBooking() throws InterruptedException {
        int numberOfThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    LessonRequestDto request = new LessonRequestDto();
                    request.setTutorId(tutor.getId());
                    request.setStartTime(startTime);
                    request.setDurationType(DurationType.SIXTY);
                    request.setAllowAlternativeTutor(false);

                    lessonService.bookLesson(request, student);
                } catch (Exception e) {
                    // 충돌 또는 예약 실패는 무시 (예외 터질 수 있음이 정상)
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 쓰레드 종료 대기
        executorService.shutdown();

        List<Lesson> lessons = lessonRepository.findAll();

        // 핵심 검증: 수업은 단 하나만 생성되어야 함
        assertThat(lessons).hasSize(1);
        System.out.println("최종 생성된 수업 수: " + lessons.size());
    }

    /**
     * [대체 튜터 매칭 테스트]
     * - 지정된 튜터는 예약 불가한 경우
     * - 대체 튜터에게 같은 시간대 슬롯이 있을 경우 정상 수업 생성
     */
    @Test
    void testAlternativeTutorBooking() {
        // 기존 tutor의 슬롯은 모두 예약된 상태로 설정
        availabilityRepository.findAll().forEach(a -> {
            a.setBooked(true);
            availabilityRepository.save(a);
        });

        // 대체 튜터 생성 및 슬롯 등록
        User altTutor = userRepository.save(User.builder()
                .name("Alt Tutor")
                .email("alt@ringle.com")
                .password("encoded")
                .role(Role.TUTOR)
                .build());

        availabilityRepository.save(Availability.builder()
                .tutor(altTutor)
                .startTime(startTime)
                .endTime(startTime.plusMinutes(30))
                .isBooked(false)
                .build());

        availabilityRepository.save(Availability.builder()
                .tutor(altTutor)
                .startTime(startTime.plusMinutes(30))
                .endTime(startTime.plusMinutes(60))
                .isBooked(false)
                .build());

        // 대체 튜터 허용 설정
        LessonRequestDto request = new LessonRequestDto();
        request.setTutorId(tutor.getId()); // 불가능한 튜터 ID
        request.setStartTime(startTime);
        request.setDurationType(DurationType.SIXTY);
        request.setAllowAlternativeTutor(true); // 대체 허용

        lessonService.bookLesson(request, student);

        List<Lesson> lessons = lessonRepository.findAll();
        assertThat(lessons).hasSize(1);
        assertThat(lessons.get(0).getTutor().getId()).isEqualTo(altTutor.getId()); // 대체 튜터가 배정되었는지 확인
    }

    /**
     * [예약 불가 예외 테스트]
     * - 지정한 튜터 슬롯이 예약되었고
     * - 대체 튜터도 존재하지 않을 경우
     * - 예외 발생
     */
    @Test
    void testBookingFailureWhenNoSlotAvailable() {
        // 모든 tutor 슬롯 예약 처리
        availabilityRepository.findAll().forEach(a -> {
            a.setBooked(true);
            availabilityRepository.save(a);
        });

        // 예약 요청 (대체 튜터도 불허)
        LessonRequestDto request = new LessonRequestDto();
        request.setTutorId(tutor.getId());
        request.setStartTime(startTime);
        request.setDurationType(DurationType.SIXTY);
        request.setAllowAlternativeTutor(false);

        // 예외 검증
        assertThatThrownBy(() -> lessonService.bookLesson(request, student))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("예약 가능한 튜터가 없습니다");
    }

    /**
     * [30분 수업 예약 테스트]
     * - 단일 30분 슬롯이 있을 경우
     * - 수업이 정상 예약되는 시나리오
     */
    @Test
    void testThirtyMinutesBooking() {
        // 60분 슬롯을 삭제하고 30분 슬롯만 남김
        availabilityRepository.deleteAll();
        availabilityRepository.save(Availability.builder()
                .tutor(tutor)
                .startTime(startTime)
                .endTime(startTime.plusMinutes(30))
                .isBooked(false)
                .build());

        LessonRequestDto request = new LessonRequestDto();
        request.setTutorId(tutor.getId());
        request.setStartTime(startTime);
        request.setDurationType(DurationType.THIRTY);
        request.setAllowAlternativeTutor(false);

        lessonService.bookLesson(request, student);

        List<Lesson> lessons = lessonRepository.findAll();
        assertThat(lessons).hasSize(1);
        assertThat(lessons.get(0).getDurationType()).isEqualTo(DurationType.THIRTY);
    }
}
