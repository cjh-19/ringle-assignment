package com.ringle.domain.lesson.entity;

import com.ringle.domain.lesson.entity.enums.DurationType;
import com.ringle.domain.lesson.entity.enums.LessonStatus;
import com.ringle.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 수업 엔티티
 * - 학생이 튜터에게 신청한 수업 정보
 * - 한 수업은 하나의 학생, 하나의 튜터와 연결
 */
@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "lessons")
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 수업 신청자 (학생)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    // 수업을 진행할 튜터
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id", nullable = false)
    private User tutor;

    // 수업 시작 및 종료 시각
    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    // 수업 길이: 30분 or 60분
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DurationType durationType;

    // 수업 상태: 신청 중 / 확정 / 취소
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LessonStatus status;

    // 수업 등록 시각
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // 생성 시 자동 설정
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}

