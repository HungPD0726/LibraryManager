package com.library.feature.borrow;

import com.library.domain.model.Borrow;

import java.time.LocalDate;
import java.util.List;

public record StudentBorrowPageView(
        List<BorrowLineView> cartItems,
        List<Borrow> borrowHistory,
        List<HoldRowView> activeHolds,
        LocalDate defaultDueDate
) {
}
