package com.library.feature.dashboard;

import com.library.domain.repository.BookRepository;
import com.library.domain.repository.BorrowItemRepository;
import com.library.domain.repository.BorrowRepository;
import com.library.domain.repository.FineRepository;
import com.library.domain.repository.OrderRepository;
import com.library.shared.constant.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final BorrowRepository borrowRepository;
    private final BookRepository bookRepository;
    private final OrderRepository orderRepository;
    private final FineRepository fineRepository;
    private final BorrowItemRepository borrowItemRepository;

    @Transactional(readOnly = true)
    public Map<Integer, Long> getMonthlyBorrowStats() {
        LocalDate startDate = LocalDate.now().minusMonths(11).withDayOfMonth(1);
        List<Object[]> rows = borrowRepository.countByMonthSince(startDate);

        Map<Integer, Long> result = new LinkedHashMap<>();
        for (int month = 1; month <= 12; month++) {
            result.put(month, 0L);
        }
        for (Object[] row : rows) {
            Integer month = ((Number) row[0]).intValue();
            Long count = ((Number) row[1]).longValue();
            result.put(month, count);
        }
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getBorrowStatusDistribution() {
        List<Object[]> rows = borrowRepository.countByStatusGroup();
        Map<String, Long> result = new LinkedHashMap<>();
        for (Object[] row : rows) {
            result.put((String) row[0], ((Number) row[1]).longValue());
        }
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getCategoryDistribution() {
        Map<String, Long> result = new LinkedHashMap<>();
        for (Object[] row : bookRepository.countByCategoryName()) {
            long count = ((Number) row[1]).longValue();
            if (count > 0) {
                result.put((String) row[0], count);
            }
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<TopBorrowedBookView> getTopBorrowedBooks(int limit) {
        List<TopBorrowedBookView> result = new ArrayList<>();
        for (Object[] row : borrowItemRepository.findTopBorrowedBooks(limit)) {
            result.add(new TopBorrowedBookView(
                    ((Number) row[0]).intValue(),
                    (String) row[1],
                    ((Number) row[2]).longValue()
            ));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<TopBorrowerView> getTopBorrowers(int limit) {
        List<TopBorrowerView> result = new ArrayList<>();
        for (Object[] row : borrowItemRepository.findTopBorrowers(limit)) {
            result.add(new TopBorrowerView(
                    ((Number) row[0]).intValue(),
                    (String) row[1],
                    ((Number) row[2]).longValue()
            ));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalRevenue() {
        return orderRepository.sumRevenueByStatus(OrderStatus.DELIVERED);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getMonthlyRevenueChart() {
        YearMonth startMonth = YearMonth.now().minusMonths(11);
        LocalDate startDate = startMonth.atDay(1);

        Map<YearMonth, BigDecimal> orderRevenueByMonth = initializeMoneyTimeline(startMonth);
        Map<YearMonth, BigDecimal> fineRevenueByMonth = initializeMoneyTimeline(startMonth);

        for (Object[] row : orderRepository.sumRevenueByMonthSince(OrderStatus.DELIVERED, startDate)) {
            YearMonth yearMonth = YearMonth.of(((Number) row[0]).intValue(), ((Number) row[1]).intValue());
            if (orderRevenueByMonth.containsKey(yearMonth)) {
                orderRevenueByMonth.put(yearMonth, toBigDecimal(row[2]));
            }
        }

        for (Object[] row : fineRepository.sumPaidAmountByMonthSince(startDate)) {
            YearMonth yearMonth = YearMonth.of(((Number) row[0]).intValue(), ((Number) row[1]).intValue());
            if (fineRevenueByMonth.containsKey(yearMonth)) {
                fineRevenueByMonth.put(yearMonth, toBigDecimal(row[2]));
            }
        }

        List<String> labels = new ArrayList<>();
        List<BigDecimal> orderRevenue = new ArrayList<>();
        List<BigDecimal> fineRevenue = new ArrayList<>();
        List<BigDecimal> totalRevenue = new ArrayList<>();

        for (int index = 0; index < 12; index++) {
            YearMonth yearMonth = startMonth.plusMonths(index);
            BigDecimal orderValue = orderRevenueByMonth.getOrDefault(yearMonth, BigDecimal.ZERO);
            BigDecimal fineValue = fineRevenueByMonth.getOrDefault(yearMonth, BigDecimal.ZERO);

            labels.add("T" + yearMonth.getMonthValue() + "/" + String.format("%02d", yearMonth.getYear() % 100));
            orderRevenue.add(orderValue);
            fineRevenue.add(fineValue);
            totalRevenue.add(orderValue.add(fineValue));
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("labels", labels);
        result.put("orderRevenue", orderRevenue);
        result.put("fineRevenue", fineRevenue);
        result.put("totalRevenue", totalRevenue);
        return result;
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalUnpaidFines() {
        return fineRepository.sumUnpaidAmount();
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalPaidFines() {
        return fineRepository.sumPaidAmount();
    }

    private Map<YearMonth, BigDecimal> initializeMoneyTimeline(YearMonth startMonth) {
        Map<YearMonth, BigDecimal> timeline = new LinkedHashMap<>();
        for (int index = 0; index < 12; index++) {
            timeline.put(startMonth.plusMonths(index), BigDecimal.ZERO);
        }
        return timeline;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        return BigDecimal.ZERO;
    }
}
