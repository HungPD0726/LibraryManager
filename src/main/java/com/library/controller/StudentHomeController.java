package com.library.controller;

import com.library.entity.Book;
import com.library.entity.Price;
import com.library.entity.Student;
import com.library.service.BookHoldService;
import com.library.service.BookService;
import com.library.service.BorrowService;
import com.library.service.CategoryService;
import com.library.service.OrderService;
import com.library.service.PublisherService;
import com.library.service.StudentContextService;
import com.library.service.StudentSessionService;
import com.library.web.view.BookCardView;
import com.library.web.view.StudentHomeSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class StudentHomeController {

    private final BookService bookService;
    private final CategoryService categoryService;
    private final PublisherService publisherService;
    private final StudentContextService studentContextService;
    private final StudentSessionService studentSessionService;
    private final BookHoldService bookHoldService;
    private final OrderService orderService;
    private final BorrowService borrowService;

    @GetMapping("/home")
    public String home(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) String search,
                       @RequestParam(required = false) String letter,
                       @RequestParam(required = false) Integer categoryId,
                       @RequestParam(required = false) Integer publisherId,
                       @RequestParam(required = false) String author,
                       Authentication authentication,
                       HttpSession session,
                       Model model) {
        Student student = studentContextService.resolveCurrentStudent(authentication)
                .orElseThrow(() -> new IllegalArgumentException("Không thể xác định hồ sơ sinh viên hiện tại."));

        Page<Book> bookPage = bookService.searchCatalog(search, letter, categoryId, publisherId, author, page, 12);
        List<BookCardView> books = bookPage.getContent().stream().map(this::toCardView).toList();

        model.addAttribute("books", books);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("publishers", publisherService.findAll());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", bookPage.getTotalPages());
        model.addAttribute("search", search);
        model.addAttribute("letter", letter);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("publisherId", publisherId);
        model.addAttribute("author", author);
        model.addAttribute("summary", new StudentHomeSummary(
                studentSessionService.borrowCart(session).size(),
                studentSessionService.waitlist(session).size(),
                borrowService.countActiveByStudent(student.getStudentId()),
                bookHoldService.countActiveByStudent(student.getStudentId()),
                orderService.findByStudent(student.getStudentId()).size(),
                borrowService.countOverdueByStudent(student.getStudentId())
        ));
        model.addAttribute("studentDisplayName", studentContextService.buildDisplayName(null, student));
        return "student/home";
    }

    private BookCardView toCardView(Book book) {
        Price price = bookService.getCurrentPriceValue(book.getBookId());
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
