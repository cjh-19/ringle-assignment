package com.ringle.domain.availability.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TimeSlotDto {
    private String time;       // "13:00"
    private boolean available; // true: 수업 가능
}