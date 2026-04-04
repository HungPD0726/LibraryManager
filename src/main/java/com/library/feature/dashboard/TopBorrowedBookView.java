package com.library.feature.dashboard;

public record TopBorrowedBookView(
        Integer bookId,
        String bookName,
        long totalBorrowed
) {
}
