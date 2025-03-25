package com.ringle.domain.availability.entity.enums;

import lombok.Getter;

/**
 * 수업 길이 옵션
 */
@Getter
public enum DurationType {
    THIRTY(30),
    SIXTY(60);

    private final int minutes;

    DurationType(int minutes) {
        this.minutes = minutes;
    }
}
