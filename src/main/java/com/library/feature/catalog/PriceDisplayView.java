package com.library.feature.catalog;

import java.math.BigDecimal;

public record PriceDisplayView(
        Integer bookId,
        String bookName,
        Integer available,
        BigDecimal amount,
        String currency,
        String note
) {
}
