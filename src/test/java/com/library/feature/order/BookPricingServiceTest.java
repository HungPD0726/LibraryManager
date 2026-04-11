package com.library.feature.order;

import com.library.domain.model.BookPrice;
import com.library.domain.model.Price;
import com.library.domain.repository.BookPriceRepository;
import com.library.feature.catalog.PriceDisplayView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookPricingServiceTest {

    @Mock
    private BookPriceRepository bookPriceRepository;

    @InjectMocks
    private BookPricingService bookPricingService;

    @Test
    void findCurrentPrice_shouldReturnLinkedPrice() {
        Price price = new Price();
        price.setPriceId(11);
        price.setAmount(new BigDecimal("125000"));

        BookPrice bookPrice = new BookPrice();
        bookPrice.setBookId(5);
        bookPrice.setPriceId(11);
        bookPrice.setStartDate(LocalDate.now());
        bookPrice.setPrice(price);

        when(bookPriceRepository.findCurrentByBookId(5)).thenReturn(Optional.of(bookPrice));

        assertThat(bookPricingService.findCurrentPrice(5)).contains(price);
    }

    @Test
    void findCurrentPrices_shouldBuildMapByBookId() {
        Price firstPrice = new Price();
        firstPrice.setPriceId(11);
        firstPrice.setAmount(new BigDecimal("125000"));

        BookPrice first = new BookPrice();
        first.setBookId(5);
        first.setPriceId(11);
        first.setStartDate(LocalDate.now());
        first.setPrice(firstPrice);

        Price secondPrice = new Price();
        secondPrice.setPriceId(12);
        secondPrice.setAmount(new BigDecimal("150000"));

        BookPrice second = new BookPrice();
        second.setBookId(6);
        second.setPriceId(12);
        second.setStartDate(LocalDate.now());
        second.setPrice(secondPrice);

        when(bookPriceRepository.findCurrentByBookIds(List.of(5, 6))).thenReturn(List.of(first, second));

        Map<Integer, Price> prices = bookPricingService.findCurrentPrices(List.of(5, 6));

        assertThat(prices).containsEntry(5, firstPrice).containsEntry(6, secondPrice);
    }

    @Test
    void findCatalogPrices_shouldReturnProjectedCatalogRows() {
        PriceDisplayView row = new PriceDisplayView(
                9,
                "Clean Code",
                3,
                new BigDecimal("180000"),
                "VND",
                "Gia hien tai"
        );

        when(bookPriceRepository.findCurrentCatalogPriceViews()).thenReturn(List.of(row));

        assertThat(bookPricingService.findCatalogPrices()).containsExactly(row);
    }
}
