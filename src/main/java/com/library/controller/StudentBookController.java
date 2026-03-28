package com.library.controller;

import com.library.entity.Book;
import com.library.entity.Price;
import com.library.service.BookFileService;
import com.library.service.BookService;
import com.library.web.view.BookDetailView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class StudentBookController {

    private final BookService bookService;
    private final BookFileService bookFileService;

    @GetMapping("/home/book")
    public String detail(@RequestParam("id") Integer id, Model model) {
        Book book = bookService.findDetailedById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách cần xem."));
        Price price = bookService.getCurrentPriceValue(id);

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
        return "student/book-detail";
    }

    @GetMapping("/home/book/file")
    public String openFile(@RequestParam("id") Integer id) {
        return "redirect:" + bookFileService.findActiveEntity(id).getFileUrl();
    }
}
