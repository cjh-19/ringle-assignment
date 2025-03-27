package com.ringle.domain.lesson.controller;

import com.ringle.domain.lesson.dto.LessonInfoResponseDto;
import com.ringle.domain.lesson.service.LessonService;
import com.ringle.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 학생이 신청한 수업 조회 API 컨트롤러
 */
@RestController
@RequestMapping("/api/student/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;

    /**
     * 학생이 신청한 모든 수업 조회
     * - 현재 로그인한 학생 기준
     * - 수업이 없을 경우 빈 배열 반환
     */
    @Operation(summary = "학생 수업 조회", description = "현재 로그인한 학생이 신청한 수업 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<?> getMyLessons(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<LessonInfoResponseDto> lessons = lessonService.getLessonsByStudent(userDetails.getUser());
        return ResponseEntity.ok(Map.of("code", 200, "data", lessons));
    }
}
