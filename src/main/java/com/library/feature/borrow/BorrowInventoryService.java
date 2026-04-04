package com.library.feature.borrow;

import com.library.domain.model.Book;
import com.library.domain.model.BorrowItem;
import com.library.domain.repository.BookRepository;
import com.library.domain.repository.BorrowItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BorrowInventoryService {

    private final BorrowItemRepository borrowItemRepository;
    private final BookRepository bookRepository;

    @Transactional
    public void decrementAvailabilityForItems(List<BorrowItem> items, String insufficientMessage) {
        Map<Integer, Book> booksById = loadBooks(
                items.stream().map(BorrowItem::getBookId).toList()
        );

        for (BorrowItem item : items) {
            validateBorrowItem(item);
            Book book = booksById.get(item.getBookId());
            if (book == null) {
                throw new IllegalArgumentException("Không tìm thấy sách ID: " + item.getBookId());
            }
            if (book.getAvailable() < item.getQuantity()) {
                throw new IllegalArgumentException("Sách '" + book.getBookName() + "' " + insufficientMessage);
            }

            book.setAvailable(book.getAvailable() - item.getQuantity());
            bookRepository.save(book);
        }
    }

    @Transactional
    public void decrementAvailabilityForBorrow(Integer borrowId) {
        List<BorrowItem> items = borrowItemRepository.findByBorrowId(borrowId);
        decrementAvailabilityForItems(items, "không đủ số lượng để duyệt.");
    }

    @Transactional
    public void restoreAvailabilityForBorrow(Integer borrowId) {
        for (BorrowItem item : borrowItemRepository.findByBorrowId(borrowId)) {
            Book book = item.getBook();
            if (book == null) {
                book = bookRepository.findById(item.getBookId())
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách ID: " + item.getBookId()));
            }
            book.setAvailable(book.getAvailable() + item.getQuantity());
            bookRepository.save(book);
        }
    }

    public void validateBorrowItem(BorrowItem item) {
        if (item == null || item.getBookId() == null || item.getQuantity() == null || item.getQuantity() <= 0) {
            throw new IllegalArgumentException("Thông tin sách mượn không hợp lệ.");
        }
    }

    private Map<Integer, Book> loadBooks(List<Integer> bookIds) {
        Map<Integer, Book> booksById = new LinkedHashMap<>();
        for (Book book : bookRepository.findAllById(bookIds)) {
            booksById.put(book.getBookId(), book);
        }
        return booksById;
    }
}
