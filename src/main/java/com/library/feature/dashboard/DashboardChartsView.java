package com.library.feature.dashboard;

public record DashboardChartsView(
        String monthlyBorrowJson,
        String categoryDistJson,
        String borrowStatusJson,
        String revenueChartJson
) {
}
