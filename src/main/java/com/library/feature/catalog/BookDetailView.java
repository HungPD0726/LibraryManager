package com.library.feature.catalog;

import java.math.BigDecimal;
import java.util.List;

public record BookDetailView(
        Integer bookId,
        String bookName,
        String categoryName,
        String publisherName,
        String imageUrl,
        Integer quantity,
        Integer available,
        BigDecimal priceAmount,
        String currency,
        String priceNote,
        String description,
        String shelfLocation,
        List<String> authors,
        List<BookFileView> files
) {
}
