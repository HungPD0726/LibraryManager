package com.library.feature.order;

import com.library.domain.model.BookPrice;
import com.library.domain.model.Price;
import com.library.domain.repository.BookPriceRepository;
import com.library.feature.catalog.PriceDisplayView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookPricingService {

    private final BookPriceRepository bookPriceRepository;

    @Transactional(readOnly = true)
    public Optional<Price> findCurrentPrice(Integer bookId) {
        if (bookId == null) {
            return Optional.empty();
        }
        return bookPriceRepository.findCurrentByBookId(bookId)
                .map(BookPrice::getPrice);
    }

    @Transactional(readOnly = true)
    public Map<Integer, Price> findCurrentPrices(Collection<Integer> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) {
            return Map.of();
        }

        Map<Integer, Price> prices = new LinkedHashMap<>();
        for (BookPrice bookPrice : bookPriceRepository.findCurrentByBookIds(bookIds)) {
            if (bookPrice.getBookId() != null && bookPrice.getPrice() != null) {
                prices.put(bookPrice.getBookId(), bookPrice.getPrice());
            }
        }
        return prices;
    }

    @Transactional(readOnly = true)
    public java.util.List<PriceDisplayView> findCatalogPrices() {
        return bookPriceRepository.findCurrentCatalogPriceViews();
    }
}
