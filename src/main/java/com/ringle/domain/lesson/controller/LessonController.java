package com.ringle.domain.lesson.controller;

import com.ringle.domain.lesson.dto.request.LessonRequestDto;
import com.ringle.domain.lesson.dto.response.LessonInfoResponseDto;
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

    /**
     * 수업 신청
     * - 학생이 수업을 예약할 수 있도록 요청 처리
     */
    @PostMapping("/book")
    @Operation(summary = "수업 신청", description = "수업 시간, 길이, 튜터 정보를 바탕으로 수업을 신청합니다.")
    public ResponseEntity<?> bookLesson(
            @RequestBody LessonRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        lessonService.bookLesson(request, userDetails.getUser());
        return ResponseEntity.ok(Map.of("code", 200, "message", "수업이 성공적으로 신청되었습니다."));
    }
}
