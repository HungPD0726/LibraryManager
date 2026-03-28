package com.library.web.view;

public record BorrowLineView(
        Integer bookId,
        String bookName,
        Integer quantity
) {
}
