package com.library.feature.catalog;

import com.library.domain.model.Book;
import com.library.domain.model.BookPrice;
import com.library.domain.model.Price;
import com.library.domain.repository.BookPriceRepository;
import com.library.domain.repository.BookRepository;
import com.library.domain.repository.PriceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;
    @Mock
    private PriceRepository priceRepository;
    @Mock
    private BookPriceRepository bookPriceRepository;

    @InjectMocks
    private BookService bookService;

    @Test
    void searchCatalog_shouldTrimBlankFiltersBeforeDelegating() {
        Page<Book> expected = new PageImpl<>(List.of());

        when(bookRepository.searchCatalog(eq("spring"), isNull(), eq(2), eq(3), eq("Martin"), any(Pageable.class)))
                .thenReturn(expected);

        Page<Book> result = bookService.searchCatalog("  spring  ", "   ", 2, 3, "  Martin ", 1, 12);

        assertThat(result).isSameAs(expected);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(bookRepository).searchCatalog(eq("spring"), isNull(), eq(2), eq(3), eq("Martin"), pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(1);
        assertThat(pageable.getPageSize()).isEqualTo(12);
        assertThat(pageable.getSort().getOrderFor("bookName")).isNotNull();
        assertThat(pageable.getSort().getOrderFor("bookName").getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void createBook_shouldCreateInitialPriceWithDefaultCurrencyWhenMissing() {
        Book book = new Book();
        book.setBookId(10);

        when(bookRepository.save(book)).thenReturn(book);
        when(bookPriceRepository.findCurrentByBookId(10)).thenReturn(Optional.empty());
        when(priceRepository.save(any(Price.class))).thenAnswer(invocation -> {
            Price price = invocation.getArgument(0);
            price.setPriceId(20);
            return price;
        });

        Book saved = bookService.createBook(book, new BigDecimal("100000"), null, "Launch price");

        assertThat(saved).isSameAs(book);

        ArgumentCaptor<Price> priceCaptor = ArgumentCaptor.forClass(Price.class);
        verify(priceRepository).save(priceCaptor.capture());
        Price createdPrice = priceCaptor.getValue();
        assertThat(createdPrice.getAmount()).isEqualByComparingTo("100000");
        assertThat(createdPrice.getCurrency()).isEqualTo("VND");
        assertThat(createdPrice.getNote()).isEqualTo("Launch price");

        ArgumentCaptor<BookPrice> linkCaptor = ArgumentCaptor.forClass(BookPrice.class);
        verify(bookPriceRepository).save(linkCaptor.capture());
        BookPrice createdLink = linkCaptor.getValue();
        assertThat(createdLink.getBookId()).isEqualTo(10);
        assertThat(createdLink.getPriceId()).isEqualTo(20);
        assertThat(createdLink.getStartDate()).isEqualTo(LocalDate.now());
        assertThat(createdLink.getEndDate()).isNull();
    }

    @Test
    void updateBook_shouldMutateCurrentPriceWhenCurrentPriceStartedToday() {
        Book book = new Book();
        book.setBookId(10);

        BookPrice currentLink = new BookPrice();
        currentLink.setBookId(10);
        currentLink.setPriceId(20);
        currentLink.setStartDate(LocalDate.now());

        Price currentPrice = new Price();
        currentPrice.setPriceId(20);
        currentPrice.setAmount(new BigDecimal("100000"));
        currentPrice.setCurrency("USD");
        currentPrice.setNote("Old note");

        when(bookRepository.save(book)).thenReturn(book);
        when(bookPriceRepository.findCurrentByBookId(10)).thenReturn(Optional.of(currentLink));
        when(priceRepository.findById(20)).thenReturn(Optional.of(currentPrice));

        bookService.updateBook(book, new BigDecimal("120000"), null, "Updated note");

        assertThat(currentPrice.getAmount()).isEqualByComparingTo("120000");
        assertThat(currentPrice.getCurrency()).isEqualTo("VND");
        assertThat(currentPrice.getNote()).isEqualTo("Updated note");

        verify(priceRepository).save(currentPrice);
        verify(bookPriceRepository, never()).save(currentLink);
    }

    @Test
    void updateBook_shouldCloseOldPriceAndCreateNewPriceWhenStartedBeforeToday() {
        Book book = new Book();
        book.setBookId(10);

        BookPrice currentLink = new BookPrice();
        currentLink.setBookId(10);
        currentLink.setPriceId(20);
        currentLink.setStartDate(LocalDate.now().minusDays(2));

        Price currentPrice = new Price();
        currentPrice.setPriceId(20);
        currentPrice.setAmount(new BigDecimal("100000"));
        currentPrice.setCurrency("VND");
        currentPrice.setNote("Base");

        when(bookRepository.save(book)).thenReturn(book);
        when(bookPriceRepository.findCurrentByBookId(10)).thenReturn(Optional.of(currentLink));
        when(priceRepository.findById(20)).thenReturn(Optional.of(currentPrice));
        when(priceRepository.save(any(Price.class))).thenAnswer(invocation -> {
            Price price = invocation.getArgument(0);
            price.setPriceId(30);
            return price;
        });

        bookService.updateBook(book, new BigDecimal("130000"), "USD", "Promo");

        ArgumentCaptor<BookPrice> linkCaptor = ArgumentCaptor.forClass(BookPrice.class);
        verify(bookPriceRepository, times(2)).save(linkCaptor.capture());
        List<BookPrice> savedLinks = linkCaptor.getAllValues();

        assertThat(savedLinks.get(0)).isSameAs(currentLink);
        assertThat(savedLinks.get(0).getEndDate()).isEqualTo(LocalDate.now().minusDays(1));

        BookPrice newLink = savedLinks.get(1);
        assertThat(newLink.getBookId()).isEqualTo(10);
        assertThat(newLink.getPriceId()).isEqualTo(30);
        assertThat(newLink.getStartDate()).isEqualTo(LocalDate.now());
        assertThat(newLink.getEndDate()).isNull();

        ArgumentCaptor<Price> priceCaptor = ArgumentCaptor.forClass(Price.class);
        verify(priceRepository).save(priceCaptor.capture());
        Price newPrice = priceCaptor.getValue();
        assertThat(newPrice.getAmount()).isEqualByComparingTo("130000");
        assertThat(newPrice.getCurrency()).isEqualTo("USD");
        assertThat(newPrice.getNote()).isEqualTo("Promo");
    }

    @Test
    void updateBook_shouldSkipPricePersistenceWhenValuesStayTheSame() {
        Book book = new Book();
        book.setBookId(10);

        BookPrice currentLink = new BookPrice();
        currentLink.setBookId(10);
        currentLink.setPriceId(20);
        currentLink.setStartDate(LocalDate.now().minusDays(3));

        Price currentPrice = new Price();
        currentPrice.setPriceId(20);
        currentPrice.setAmount(new BigDecimal("100000"));
        currentPrice.setCurrency("VND");
        currentPrice.setNote("Base");

        when(bookRepository.save(book)).thenReturn(book);
        when(bookPriceRepository.findCurrentByBookId(10)).thenReturn(Optional.of(currentLink));
        when(priceRepository.findById(20)).thenReturn(Optional.of(currentPrice));

        bookService.updateBook(book, new BigDecimal("100000"), "VND", "Base");

        verify(priceRepository, never()).save(any(Price.class));
        verify(bookPriceRepository, never()).save(any(BookPrice.class));
    }
}
