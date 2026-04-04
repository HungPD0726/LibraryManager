package com.library.feature.borrow;

public record BorrowLineView(
        Integer bookId,
        String bookName,
        Integer quantity
) {
}
