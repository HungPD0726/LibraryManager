package com.library.feature.student;

import com.library.domain.model.Book;
import com.library.domain.model.Price;
import com.library.domain.model.Student;
import com.library.feature.borrow.BookHoldService;
import com.library.feature.borrow.BorrowQueryService;
import com.library.feature.catalog.BookCardView;
import com.library.feature.catalog.BookService;
import com.library.feature.catalog.CategoryService;
import com.library.feature.catalog.PublisherService;
import com.library.feature.order.BookPricingService;
import com.library.feature.order.OrderQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StudentHomeReadService {

    private final BookService bookService;
    private final BookPricingService bookPricingService;
    private final CategoryService categoryService;
    private final PublisherService publisherService;
    private final StudentSessionService studentSessionService;
    private final BookHoldService bookHoldService;
    private final OrderQueryService orderQueryService;
    private final BorrowQueryService borrowQueryService;
    private final StudentProfileService studentProfileService;

    @Transactional(readOnly = true)
    public StudentHomePageView buildPage(Student student,
                                         String search,
                                         String letter,
                                         Integer categoryId,
                                         Integer publisherId,
                                         String author,
                                         int page,
                                         jakarta.servlet.http.HttpSession session) {
        Page<Book> bookPage = bookService.searchCatalog(search, letter, categoryId, publisherId, author, page, 12);
        Map<Integer, Price> pricesByBookId = bookPricingService.findCurrentPrices(
                bookPage.getContent().stream().map(Book::getBookId).toList()
        );

        List<BookCardView> books = bookPage.getContent().stream()
                .map(book -> toCardView(book, pricesByBookId.get(book.getBookId())))
                .toList();

        StudentHomeSummary summary = new StudentHomeSummary(
                studentSessionService.borrowCart(session).size(),
                studentSessionService.waitlist(session).size(),
                borrowQueryService.countActiveByStudent(student.getStudentId()),
                bookHoldService.countActiveByStudent(student.getStudentId()),
                orderQueryService.countByStudent(student.getStudentId()),
                borrowQueryService.countOverdueByStudent(student.getStudentId())
        );

        return new StudentHomePageView(
                books,
                categoryService.findAllOptions(),
                publisherService.findAllOptions(),
                page,
                bookPage.getTotalPages(),
                search,
                letter,
                categoryId,
                publisherId,
                author,
                summary,
                studentProfileService.buildDisplayName(null, student)
        );
    }

    private BookCardView toCardView(Book book, Price price) {
        return new BookCardView(
                book.getBookId(),
                book.getBookName(),
                book.getCategory() != null ? book.getCategory().getCategoryName() : null,
                book.getPublisher() != null ? book.getPublisher().getPublisherName() : null,
                book.getImageUrl(),
                book.getAvailable(),
                price != null ? price.getAmount() : null,
                price != null ? price.getCurrency() : null,
                book.getAuthors().stream().map(author -> author.getAuthorName()).toList()
        );
    }
}
