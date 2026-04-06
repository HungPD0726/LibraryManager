package com.library.feature.dashboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.feature.borrow.BorrowQueryService;
import com.library.feature.catalog.BookService;
import com.library.feature.catalog.CategoryService;
import com.library.feature.fine.FineService;
import com.library.feature.staff.StaffService;
import com.library.feature.student.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardReadService {

    private final BookService bookService;
    private final StudentService studentService;
    private final StaffService staffService;
    private final BorrowQueryService borrowQueryService;
    private final CategoryService categoryService;
    private final FineService fineService;
    private final StatisticsService statisticsService;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public DashboardPageView read() {
        DashboardChartsView charts = buildCharts();
        return new DashboardPageView(
                bookService.countAll(),
                bookService.countTotalAvailable(),
                studentService.count(),
                staffService.count(),
                categoryService.count(),
                borrowQueryService.countPending(),
                borrowQueryService.countBorrowing(),
                borrowQueryService.countOverdue(),
                fineService.countUnpaid(),
                fineService.sumUnpaidAmount(),
                fineService.sumPaidAmount(),
                statisticsService.getTotalRevenue(),
                charts,
                statisticsService.getTopBorrowedBooks(10),
                statisticsService.getTopBorrowers(5)
        );
    }

    private DashboardChartsView buildCharts() {
        try {
            return new DashboardChartsView(
                    objectMapper.writeValueAsString(statisticsService.getMonthlyBorrowStats()),
                    objectMapper.writeValueAsString(statisticsService.getCategoryDistribution()),
                    objectMapper.writeValueAsString(statisticsService.getBorrowStatusDistribution()),
                    objectMapper.writeValueAsString(statisticsService.getMonthlyRevenueChart())
            );
        } catch (Exception ex) {
            return new DashboardChartsView("{}", "{}", "{}", "{}");
        }
    }
}
