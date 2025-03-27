package com.ringle.domain.availability.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TutorSlotDto {
    private Long tutorId;
    private String tutorName;
    private List<String> availableTimes; // ["13:00", "13:30"]
}