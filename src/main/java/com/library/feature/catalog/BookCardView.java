package com.library.feature.catalog;

import java.math.BigDecimal;
import java.util.List;

public record BookCardView(
        Integer bookId,
        String bookName,
        String categoryName,
        String publisherName,
        String imageUrl,
        Integer available,
        BigDecimal priceAmount,
        String currency,
        List<String> authors
) {
}
