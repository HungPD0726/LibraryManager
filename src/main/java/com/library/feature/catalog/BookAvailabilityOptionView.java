package com.library.feature.catalog;

public record BookAvailabilityOptionView(
        Integer bookId,
        String bookName,
        Integer available
) {
}
