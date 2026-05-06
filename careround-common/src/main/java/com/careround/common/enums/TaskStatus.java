package com.careround.common.enums;

/** Forward-only transitions: PENDING → IN_PROGRESS → COMPLETED. */
public enum TaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    OVERDUE,
    CANCELLED
}
