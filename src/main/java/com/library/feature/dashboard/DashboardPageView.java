package com.library.feature.dashboard;

import java.math.BigDecimal;
import java.util.List;

public record DashboardPageView(
        long totalBooks,
        long totalAvailable,
        long totalStudents,
        long totalStaff,
        long totalCategories,
        long pendingBorrows,
        long activeBorrows,
        long overdueBorrows,
        long unpaidFines,
        BigDecimal unpaidFineTotal,
        BigDecimal paidFineTotal,
        BigDecimal totalRevenue,
        DashboardChartsView charts,
        List<TopBorrowedBookView> topBooks,
        List<TopBorrowerView> topBorrowers
) {
}
