package com.library.feature.order;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record OrderRowView(
        Integer orderId,
        String studentName,
        String staffName,
        LocalDate orderDate,
        BigDecimal totalAmount,
        String status,
        List<OrderItemView> items
) {
}
