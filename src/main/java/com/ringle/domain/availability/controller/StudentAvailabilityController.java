package com.ringle.domain.availability.controller;

import com.ringle.domain.availability.dto.response.TimeSlotDto;
import com.ringle.domain.availability.dto.response.TutorSlotDto;
import com.ringle.domain.availability.service.StudentAvailabilityService;
import com.ringle.domain.lesson.entity.enums.DurationType;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 학생 전용 수업 가능 시간 조회 API
 * - 날짜/수업 길이 기반 시간대 조회
 * - 튜터별 수업 가능 시간 조회
 */
@RestController
@RequestMapping("/api/student/availability")
@RequiredArgsConstructor
public class StudentAvailabilityController {

    private final StudentAvailabilityService studentAvailabilityService;

    /**
     * 날짜별 수업 가능 시간대 조회
     * - 30분 단위 시간 슬롯을 반환 (가능 여부 포함)
     * - 60분 수업의 경우 연속된 슬롯 필요
     */
    @Operation(summary = "수업 가능 시간대 조회", description = "입력한 날짜와 수업 길이에 따라 수업 가능한 시간대 목록을 반환합니다.")
    @GetMapping("/slots")
    public ResponseEntity<?> getAvailableTimeSlots(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @NotNull DurationType durationType
    ) {
        List<TimeSlotDto> result = studentAvailabilityService.getAvailableTimeSlots(date, durationType);
        return ResponseEntity.ok(Map.of("code", 200, "data", result));
    }

    /**
     * 특정 날짜 기준 튜터별 수업 가능 시간대 조회
     * - 예약되지 않은 수업 시간 기준
     */
    @Operation(summary = "튜터별 수업 가능 시간 조회", description = "입력한 날짜 기준으로 수업 가능한 튜터 목록과 시간대를 반환합니다.")
    @GetMapping("/tutors")
    public ResponseEntity<?> getTutorSlotsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<TutorSlotDto> result = studentAvailabilityService.getTutorAvailableSlotsByDate(date);
        return ResponseEntity.ok(Map.of("code", 200, "data", result));
    }
}
