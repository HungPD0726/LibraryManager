package com.library.feature.dashboard;

public record TopBorrowerView(
        Integer studentId,
        String studentName,
        long totalBorrowed
) {
}
