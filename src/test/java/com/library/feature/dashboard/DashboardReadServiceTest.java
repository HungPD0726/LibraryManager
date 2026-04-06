package com.library.feature.dashboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.feature.borrow.BorrowQueryService;
import com.library.feature.catalog.BookService;
import com.library.feature.catalog.CategoryService;
import com.library.feature.fine.FineService;
import com.library.feature.staff.StaffService;
import com.library.feature.student.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DashboardReadServiceTest {

    private final FakeBookService bookService = new FakeBookService();
    private final FakeStudentService studentService = new FakeStudentService();
    private final FakeStaffService staffService = new FakeStaffService();
    private final FakeBorrowQueryService borrowQueryService = new FakeBorrowQueryService();
    private final FakeCategoryService categoryService = new FakeCategoryService();
    private final FakeFineService fineService = new FakeFineService();
    private final FakeStatisticsService statisticsService = new FakeStatisticsService();

    private DashboardReadService dashboardReadService;

    @BeforeEach
    void setUp() {
        dashboardReadService = new DashboardReadService(
                bookService,
                studentService,
                staffService,
                borrowQueryService,
                categoryService,
                fineService,
                statisticsService,
                new ObjectMapper()
        );
    }

    @Test
    void read_shouldAggregateDashboardMetricsAndTypedViews() {
        bookService.totalBooks = 100L;
        bookService.totalAvailable = 70L;
        studentService.countValue = 40L;
        staffService.countValue = 8L;
        categoryService.countValue = 6L;
        borrowQueryService.pendingCount = 3L;
        borrowQueryService.borrowingCount = 5L;
        borrowQueryService.overdueCount = 2L;
        fineService.unpaidCount = 4L;
        fineService.unpaidAmount = new BigDecimal("120000");
        fineService.paidAmount = new BigDecimal("340000");
        statisticsService.totalRevenue = new BigDecimal("560000");
        statisticsService.monthlyStats = new LinkedHashMap<>();
        statisticsService.monthlyStats.put(1, 4L);
        statisticsService.monthlyStats.put(2, 6L);
        statisticsService.categoryDistribution = new LinkedHashMap<>();
        statisticsService.categoryDistribution.put("Java", 7L);
        statisticsService.borrowStatusDistribution = new LinkedHashMap<>();
        statisticsService.borrowStatusDistribution.put("BORROWING", 5L);
        statisticsService.revenueChart = new LinkedHashMap<>();
        statisticsService.revenueChart.put("labels", List.of("T3/26", "T4/26"));
        statisticsService.revenueChart.put("orderRevenue", List.of(new BigDecimal("120000"), new BigDecimal("140000")));
        statisticsService.revenueChart.put("fineRevenue", List.of(new BigDecimal("10000"), new BigDecimal("20000")));
        statisticsService.revenueChart.put("totalRevenue", List.of(new BigDecimal("130000"), new BigDecimal("160000")));
        statisticsService.topBooks = List.of(new TopBorrowedBookView(1, "Clean Code", 9L));
        statisticsService.topBorrowers = List.of(new TopBorrowerView(2, "Nguyen Van A", 4L));

        DashboardPageView pageView = dashboardReadService.read();

        assertThat(pageView.totalBooks()).isEqualTo(100L);
        assertThat(pageView.pendingBorrows()).isEqualTo(3L);
        assertThat(pageView.totalRevenue()).isEqualByComparingTo("560000");
        assertThat(pageView.topBooks()).containsExactlyElementsOf(statisticsService.topBooks);
        assertThat(pageView.topBorrowers()).containsExactlyElementsOf(statisticsService.topBorrowers);
        assertThat(pageView.charts().monthlyBorrowJson()).isEqualTo("{\"1\":4,\"2\":6}");
        assertThat(pageView.charts().categoryDistJson()).isEqualTo("{\"Java\":7}");
        assertThat(pageView.charts().borrowStatusJson()).isEqualTo("{\"BORROWING\":5}");
        assertThat(pageView.charts().revenueChartJson())
                .isEqualTo("{\"labels\":[\"T3/26\",\"T4/26\"],\"orderRevenue\":[120000,140000],\"fineRevenue\":[10000,20000],\"totalRevenue\":[130000,160000]}");
    }

    private static final class FakeBookService extends BookService {
        private long totalBooks;
        private long totalAvailable;

        private FakeBookService() {
            super(null, null, null);
        }

        @Override
        public long countAll() {
            return totalBooks;
        }

        @Override
        public long countTotalAvailable() {
            return totalAvailable;
        }
    }

    private static final class FakeStudentService extends StudentService {
        private long countValue;

        private FakeStudentService() {
            super(null);
        }

        @Override
        public long count() {
            return countValue;
        }
    }

    private static final class FakeStaffService extends StaffService {
        private long countValue;

        private FakeStaffService() {
            super(null, null, null);
        }

        @Override
        public long count() {
            return countValue;
        }
    }

    private static final class FakeBorrowQueryService extends BorrowQueryService {
        private long pendingCount;
        private long borrowingCount;
        private long overdueCount;

        private FakeBorrowQueryService() {
            super(null, null);
        }

        @Override
        public long countPending() {
            return pendingCount;
        }

        @Override
        public long countBorrowing() {
            return borrowingCount;
        }

        @Override
        public long countOverdue() {
            return overdueCount;
        }
    }

    private static final class FakeCategoryService extends CategoryService {
        private long countValue;

        private FakeCategoryService() {
            super(null);
        }

        @Override
        public long count() {
            return countValue;
        }
    }

    private static final class FakeFineService extends FineService {
        private long unpaidCount;
        private BigDecimal unpaidAmount = BigDecimal.ZERO;
        private BigDecimal paidAmount = BigDecimal.ZERO;

        private FakeFineService() {
            super(null, null);
        }

        @Override
        public long countUnpaid() {
            return unpaidCount;
        }

        @Override
        public BigDecimal sumUnpaidAmount() {
            return unpaidAmount;
        }

        @Override
        public BigDecimal sumPaidAmount() {
            return paidAmount;
        }
    }

    private static final class FakeStatisticsService extends StatisticsService {
        private Map<Integer, Long> monthlyStats = Map.of();
        private Map<String, Long> categoryDistribution = Map.of();
        private Map<String, Long> borrowStatusDistribution = Map.of();
        private Map<String, Object> revenueChart = Map.of();
        private BigDecimal totalRevenue = BigDecimal.ZERO;
        private List<TopBorrowedBookView> topBooks = List.of();
        private List<TopBorrowerView> topBorrowers = List.of();

        private FakeStatisticsService() {
            super(null, null, null, null, null);
        }

        @Override
        public Map<Integer, Long> getMonthlyBorrowStats() {
            return monthlyStats;
        }

        @Override
        public Map<String, Long> getCategoryDistribution() {
            return categoryDistribution;
        }

        @Override
        public Map<String, Long> getBorrowStatusDistribution() {
            return borrowStatusDistribution;
        }

        @Override
        public Map<String, Object> getMonthlyRevenueChart() {
            return revenueChart;
        }

        @Override
        public BigDecimal getTotalRevenue() {
            return totalRevenue;
        }

        @Override
        public List<TopBorrowedBookView> getTopBorrowedBooks(int limit) {
            return topBooks;
        }

        @Override
        public List<TopBorrowerView> getTopBorrowers(int limit) {
            return topBorrowers;
        }
    }
}
