package com.library.web.view;

public record StudentHomeSummary(
        int borrowCartSize,
        int waitlistSize,
        long activeBorrowCount,
        long holdCount,
        long orderCount,
        long overdueCount
) {
}
