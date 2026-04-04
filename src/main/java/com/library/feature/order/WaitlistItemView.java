package com.library.feature.order;

import java.math.BigDecimal;

public record WaitlistItemView(
        Integer bookId,
        String bookName,
        Integer quantity,
        BigDecimal amount,
        BigDecimal lineTotal
) {
}
