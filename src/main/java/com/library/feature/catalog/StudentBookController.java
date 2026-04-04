package com.library.feature.catalog;

import com.library.domain.model.Book;
import com.library.domain.model.Price;
import com.library.domain.model.Student;
import com.library.feature.order.BookPricingService;
import com.library.feature.student.CurrentStudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class StudentBookController {

    private final BookService bookService;
    private final BookPricingService bookPricingService;
    private final BookFileService bookFileService;
    private final BookReviewService bookReviewService;
    private final CurrentStudentService currentStudentService;

    @GetMapping("/home/book")
    public String detail(@RequestParam("id") Integer id, Authentication authentication, Model model) {
        Book book = bookService.findDetailedById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách cần xem."));
        Price price = bookPricingService.findCurrentPrice(id).orElse(null);

        model.addAttribute("bookDetail", new BookDetailView(
                book.getBookId(),
                book.getBookName(),
                book.getCategory() != null ? book.getCategory().getCategoryName() : null,
                book.getPublisher() != null ? book.getPublisher().getPublisherName() : null,
                book.getImageUrl(),
                book.getQuantity(),
                book.getAvailable(),
                price != null ? price.getAmount() : null,
                price != null ? price.getCurrency() : null,
                price != null ? price.getNote() : null,
                book.getDescription(),
                book.getShelfLocation(),
                book.getAuthors().stream().map(author -> author.getAuthorName()).toList(),
                bookFileService.findActiveByBook(id)
        ));
        model.addAttribute("reviews", bookReviewService.findByBookId(id));
        model.addAttribute("averageRating", bookReviewService.getAverageRating(id));
        model.addAttribute("reviewCount", bookReviewService.getReviewCount(id));
        currentStudentService.resolveCurrentStudent(authentication).ifPresent(student ->
                model.addAttribute("hasReviewed", bookReviewService.hasReviewed(id, student.getStudentId()))
        );
        return "student/book-detail";
    }

    @PostMapping("/home/book/review")
    public String addReview(@RequestParam Integer bookId,
                            @RequestParam Integer rating,
                            @RequestParam(required = false) String comment,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        Student student = currentStudentService.resolveCurrentStudent(authentication)
                .orElseThrow(() -> new IllegalArgumentException("Không thể xác định sinh viên hiện tại."));

        bookReviewService.addReview(bookId, student.getStudentId(), rating, comment);
        redirectAttributes.addFlashAttribute("msg", "Cảm ơn bạn đã đánh giá!");
        return "redirect:/home/book?id=" + bookId;
    }

    @GetMapping("/home/book/file")
    public String openFile(@RequestParam("id") Integer id) {
        return "redirect:" + bookFileService.findActiveEntity(id).getFileUrl();
    }
}
