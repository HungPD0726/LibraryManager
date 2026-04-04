package com.library.feature.borrow;

import java.time.LocalDate;
import java.util.List;

public record BorrowRowView(
        Integer borrowId,
        String studentName,
        String staffName,
        LocalDate borrowDate,
        LocalDate dueDate,
        LocalDate returnDate,
        String status,
        List<BorrowLineView> items
) {
}
