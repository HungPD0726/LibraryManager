package com.library.feature.borrow;

import com.library.domain.model.Book;
import com.library.feature.catalog.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StudentBorrowReadService {

    private final BorrowQueryService borrowQueryService;
    private final BookService bookService;
    private final BookHoldService bookHoldService;

    @Transactional(readOnly = true)
    public StudentBorrowPageView buildPage(Integer studentId, Map<Integer, Integer> cart) {
        return new StudentBorrowPageView(
                buildCartViews(cart == null ? Map.of() : cart),
                borrowQueryService.findStudentHistory(studentId),
                bookHoldService.findActiveByStudent(studentId),
                LocalDate.now().plusDays(14)
        );
    }

    private List<BorrowLineView> buildCartViews(Map<Integer, Integer> cart) {
        Map<Integer, Book> booksById = new LinkedHashMap<>();
        for (Book book : bookService.findAllByIds(cart.keySet())) {
            booksById.put(book.getBookId(), book);
        }

        return cart.entrySet().stream()
                .map(entry -> {
                    Book book = booksById.get(entry.getKey());
                    if (book == null) {
                        throw new IllegalArgumentException("Không tìm thấy sách trong giỏ mượn.");
                    }
                    return new BorrowLineView(book.getBookId(), book.getBookName(), entry.getValue());
                })
                .toList();
    }
}
