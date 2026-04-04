package com.library.feature.borrow;

import com.library.domain.model.Book;
import com.library.domain.model.BorrowItem;
import com.library.domain.repository.BookRepository;
import com.library.domain.repository.BorrowItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BorrowInventoryServiceTest {

    @Mock
    private BorrowItemRepository borrowItemRepository;
    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BorrowInventoryService borrowInventoryService;

    @Test
    void decrementAvailabilityForItems_shouldUpdateBookAvailability() {
        Book book = new Book();
        book.setBookId(9);
        book.setBookName("Refactoring");
        book.setAvailable(3);

        BorrowItem item = new BorrowItem();
        item.setBookId(9);
        item.setQuantity(2);

        when(bookRepository.findAllById(List.of(9))).thenReturn(List.of(book));

        borrowInventoryService.decrementAvailabilityForItems(List.of(item), "khÃ´ng Ä‘á»§.");

        assertThat(book.getAvailable()).isEqualTo(1);
        verify(bookRepository).save(book);
    }

    @Test
    void restoreAvailabilityForBorrow_shouldAddQuantityBack() {
        Book book = new Book();
        book.setBookId(4);
        book.setBookName("Patterns of Enterprise Application Architecture");
        book.setAvailable(0);

        BorrowItem item = new BorrowItem();
        item.setBorrowId(77);
        item.setBookId(4);
        item.setQuantity(2);
        item.setBook(book);

        when(borrowItemRepository.findByBorrowId(77)).thenReturn(List.of(item));

        borrowInventoryService.restoreAvailabilityForBorrow(77);

        assertThat(book.getAvailable()).isEqualTo(2);
        verify(bookRepository).save(book);
    }
}
