package com.library.shared.realtime;

public record AdminLiveEvent(
        String type,
        String title,
        String message,
        String href,
        String tone,
        long pendingBorrowCount,
        String occurredAt
) {
}
