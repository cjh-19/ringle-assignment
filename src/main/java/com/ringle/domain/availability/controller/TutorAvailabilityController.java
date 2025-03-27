package com.ringle.domain.availability.controller;

import com.ringle.domain.availability.dto.request.AvailabilityRequestDto;
import com.ringle.domain.availability.dto.response.AvailabilityResponseDto;
import com.ringle.domain.availability.service.AvailabilityService;
import com.ringle.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 튜터용 수업 가능 시간 등록/삭제 API
 */
@RestController
@RequestMapping("/api/tutor/availabilities")
@RequiredArgsConstructor
public class TutorAvailabilityController {

    private final AvailabilityService availabilityService;

    /**
     * 수업 가능 시간 등록
     */
    @Operation(summary = "수업 가능 시간 등록", description = "튜터가 본인의 수업 가능한 시간대를 등록합니다.")
    @PostMapping
    public ResponseEntity<?> create(
            @Valid @RequestBody AvailabilityRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        availabilityService.createAvailability(request, userDetails.getUser());
        return ResponseEntity.ok(Map.of("code", 200, "message", "수업 가능 시간이 등록되었습니다."));
    }

    /**
     * 수업 가능 시간 삭제
     */
    @Operation(summary = "수업 가능 시간 삭제", description = "튜터가 예약되지 않은 수업 가능 시간을 삭제합니다.")
    @DeleteMapping("/{availabilityId}")
    public ResponseEntity<?> delete(
            @PathVariable Long availabilityId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        availabilityService.deleteAvailability(availabilityId, userDetails.getUser());
        return ResponseEntity.ok(Map.of("code", 200, "message", "수업 가능 시간이 삭제되었습니다."));
    }

    /**
     * 튜터가 등록한 수업 가능 시간 조회
     * - 본인의 모든 등록된 시간대 반환
     * - 정렬: 시작 시간 오름차순
     */
    @Operation(summary = "내 수업 가능 시간 조회", description = "튜터가 등록한 수업 가능 시간 목록을 반환합니다.")
    @GetMapping
    public ResponseEntity<?> getMyAvailabilities(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<AvailabilityResponseDto> result = availabilityService.getMyAvailabilities(userDetails.getUser());
        return ResponseEntity.ok(Map.of("code", 200, "data", result));
    }
}
