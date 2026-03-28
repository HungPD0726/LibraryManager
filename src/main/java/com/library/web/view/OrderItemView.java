package com.library.web.view;

import java.math.BigDecimal;

public record OrderItemView(
        Integer bookId,
        String bookName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {
}
