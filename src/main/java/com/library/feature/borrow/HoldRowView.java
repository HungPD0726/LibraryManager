package com.library.feature.borrow;

import java.time.LocalDateTime;

public record HoldRowView(
        Integer holdId,
        Integer bookId,
        String bookName,
        String status,
        LocalDateTime holdDate,
        LocalDateTime expireDate,
        String note
) {
}
