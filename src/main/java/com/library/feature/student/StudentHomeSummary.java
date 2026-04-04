package com.library.feature.student;

public record StudentHomeSummary(
        int borrowCartSize,
        int waitlistSize,
        long activeBorrowCount,
        long holdCount,
        long orderCount,
        long overdueCount
) {
}
