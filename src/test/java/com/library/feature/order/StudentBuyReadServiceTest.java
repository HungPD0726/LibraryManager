package com.library.feature.order;

import com.library.feature.catalog.PriceDisplayView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class StudentBuyReadServiceTest {

    @Test
    void buildPage_shouldComposeWaitlistUsingProjectedCatalogPrices() {
        FakeBookPricingService bookPricingService = new FakeBookPricingService();
        FakeOrderQueryService orderQueryService = new FakeOrderQueryService();
        StudentBuyReadService service = new StudentBuyReadService(bookPricingService, orderQueryService);
        PriceDisplayView cleanCode = new PriceDisplayView(
                9,
                "Clean Code",
                3,
                new BigDecimal("180000"),
                "VND",
                "Gia hien tai"
        );
        PriceDisplayView refactoring = new PriceDisplayView(
                10,
                "Refactoring",
                2,
                new BigDecimal("220000"),
                "VND",
                null
        );
        OrderRowView order = new OrderRowView(
                31,
                "Nguyen Minh",
                "Thu thu A",
                LocalDate.of(2026, 4, 4),
                new BigDecimal("180000"),
                "Pending",
                List.of()
        );

        Map<Integer, Integer> waitlist = new LinkedHashMap<>();
        waitlist.put(9, 2);
        waitlist.put(99, 1);

        bookPricingService.willReturn(List.of(cleanCode, refactoring));
        orderQueryService.willReturn(List.of(order));

        StudentBuyPageView view = service.buildPage(7, waitlist);

        assertThat(view.bookPrices()).containsExactly(cleanCode, refactoring);
        assertThat(view.waitlistItems()).containsExactly(
                new WaitlistItemView(
                        9,
                        "Clean Code",
                        2,
                        new BigDecimal("180000"),
                        new BigDecimal("360000")
                )
        );
        assertThat(view.orderHistory()).containsExactly(order);
    }

    private static final class FakeBookPricingService extends BookPricingService {

        private List<PriceDisplayView> rows = List.of();

        private FakeBookPricingService() {
            super(null);
        }

        @Override
        public List<PriceDisplayView> findCatalogPrices() {
            return rows;
        }

        private void willReturn(List<PriceDisplayView> rows) {
            this.rows = rows;
        }
    }

    private static final class FakeOrderQueryService extends OrderQueryService {

        private List<OrderRowView> rows = List.of();

        private FakeOrderQueryService() {
            super(null, null);
        }

        @Override
        public List<OrderRowView> findByStudent(Integer studentId) {
            return rows;
        }

        private void willReturn(List<OrderRowView> rows) {
            this.rows = rows;
        }
    }
}
