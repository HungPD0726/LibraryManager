package com.library.feature.order;

import com.library.feature.catalog.PriceDisplayView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class StudentBuyReadService {

    private final BookPricingService bookPricingService;
    private final OrderQueryService orderQueryService;

    @Transactional(readOnly = true)
    public StudentBuyPageView buildPage(Integer studentId, Map<Integer, Integer> waitlist) {
        List<PriceDisplayView> bookPrices = bookPricingService.findCatalogPrices();

        Map<Integer, PriceDisplayView> priceViewsByBookId = new LinkedHashMap<>();
        for (PriceDisplayView view : bookPrices) {
            priceViewsByBookId.put(view.bookId(), view);
        }

        List<WaitlistItemView> waitlistItems = (waitlist == null ? Map.<Integer, Integer>of() : waitlist).entrySet().stream()
                .map(entry -> {
                    PriceDisplayView price = priceViewsByBookId.get(entry.getKey());
                    if (price == null) {
                        return null;
                    }
                    BigDecimal lineTotal = price.amount().multiply(BigDecimal.valueOf(entry.getValue()));
                    return new WaitlistItemView(
                            price.bookId(),
                            price.bookName(),
                            entry.getValue(),
                            price.amount(),
                            lineTotal
                    );
                })
                .filter(Objects::nonNull)
                .toList();

        return new StudentBuyPageView(
                bookPrices,
                waitlistItems,
                orderQueryService.findByStudent(studentId)
        );
    }
}
