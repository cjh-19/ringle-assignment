package com.ringle.domain.availability.entity;

import com.ringle.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 튜터 수업 가능 시간 엔티티
 */
@Entity
@Table(name = "availabilities")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Availability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 튜터
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id")
    private User tutor;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private boolean isBooked; // 이미 예약된 시간인지 여부

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public void setBooked(boolean booked) {
        this.isBooked = booked;
    }
}
