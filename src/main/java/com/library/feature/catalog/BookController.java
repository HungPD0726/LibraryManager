package com.library.feature.catalog;

import com.library.domain.model.Book;
import com.library.domain.model.Category;
import com.library.domain.model.Price;
import com.library.domain.model.Publisher;
import com.library.feature.catalog.AuthorService;
import com.library.feature.catalog.BookService;
import com.library.feature.catalog.CategoryService;
import com.library.feature.catalog.PublisherService;
import com.library.feature.catalog.BookForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashSet;

@Controller
@RequestMapping("/admin/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final CategoryService categoryService;
    private final PublisherService publisherService;
    private final AuthorService authorService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) String search,
                       Model model) {
        Page<Book> bookPage = (search != null && !search.isBlank())
                ? bookService.searchByName(search, page, 10)
                : bookService.findAll(page, 10);

        model.addAttribute("books", bookPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", bookPage.getTotalPages());
        model.addAttribute("totalItems", bookPage.getTotalElements());
        model.addAttribute("search", search);
        return "admin/book/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        prepareForm(model, new BookForm(), null);
        return "admin/book/create";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("form") BookForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        validateAvailable(form, bindingResult);
        if (bindingResult.hasErrors()) {
            prepareForm(model, form, null);
            return "admin/book/create";
        }

        Book book = new Book();
        applyBook(book, form);
        bookService.createBook(book, form.getPriceAmount(), form.getCurrency(), form.getPriceNote());
        redirectAttributes.addFlashAttribute("msg", "ThÃƒÂªm sÃƒÂ¡ch thÃƒÂ nh cÃƒÂ´ng.");
        return "redirect:/admin/books";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        Book book = bookService.findDetailedById(id)
                .orElseThrow(() -> new IllegalArgumentException("KhÃƒÂ´ng tÃƒÂ¬m thÃ¡ÂºÂ¥y sÃƒÂ¡ch cÃ¡ÂºÂ§n chÃ¡Â»â€°nh sÃ¡Â»Â­a."));

        BookForm form = new BookForm();
        form.setBookName(book.getBookName());
        form.setQuantity(book.getQuantity());
        form.setAvailable(book.getAvailable());
        form.setCategoryId(book.getCategory().getCategoryId());
        form.setPublisherId(book.getPublisher().getPublisherId());
        form.setImageUrl(book.getImageUrl());
        form.setAuthorIds(book.getAuthors().stream().map(author -> author.getAuthorId()).toList());

        Price currentPrice = bookService.getCurrentPriceValue(id);
        if (currentPrice != null) {
            form.setPriceAmount(currentPrice.getAmount());
            form.setCurrency(currentPrice.getCurrency());
            form.setPriceNote(currentPrice.getNote());
        }

        prepareForm(model, form, book);
        return "admin/book/edit";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Integer id,
                       @Valid @ModelAttribute("form") BookForm form,
                       BindingResult bindingResult,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        validateAvailable(form, bindingResult);
        Book existing = bookService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("KhÃƒÂ´ng tÃƒÂ¬m thÃ¡ÂºÂ¥y sÃƒÂ¡ch cÃ¡ÂºÂ§n cÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t."));

        if (bindingResult.hasErrors()) {
            prepareForm(model, form, existing);
            return "admin/book/edit";
        }

        applyBook(existing, form);
        bookService.updateBook(existing, form.getPriceAmount(), form.getCurrency(), form.getPriceNote());
        redirectAttributes.addFlashAttribute("msg", "CÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t sÃƒÂ¡ch thÃƒÂ nh cÃƒÂ´ng.");
        return "redirect:/admin/books";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        bookService.deleteBook(id);
        redirectAttributes.addFlashAttribute("msg", "XÃƒÂ³a sÃƒÂ¡ch thÃƒÂ nh cÃƒÂ´ng.");
        return "redirect:/admin/books";
    }

    private void prepareForm(Model model, BookForm form, Book book) {
        model.addAttribute("form", form);
        model.addAttribute("book", book);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("publishers", publisherService.findAll());
        model.addAttribute("authors", authorService.findAll());
    }

    private void applyBook(Book book, BookForm form) {
        Category category = categoryService.findById(form.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("KhÃƒÂ´ng tÃƒÂ¬m thÃ¡ÂºÂ¥y danh mÃ¡Â»Â¥c Ã„â€˜ÃƒÂ£ chÃ¡Â»Ân."));
        Publisher publisher = publisherService.findById(form.getPublisherId())
                .orElseThrow(() -> new IllegalArgumentException("KhÃƒÂ´ng tÃƒÂ¬m thÃ¡ÂºÂ¥y nhÃƒÂ  xuÃ¡ÂºÂ¥t bÃ¡ÂºÂ£n Ã„â€˜ÃƒÂ£ chÃ¡Â»Ân."));

        book.setBookName(form.getBookName().trim());
        book.setQuantity(form.getQuantity());
        book.setAvailable(form.getAvailable());
        book.setCategory(category);
        book.setPublisher(publisher);
        book.setImageUrl(blankToNull(form.getImageUrl()));
        book.setDescription(blankToNull(form.getDescription()));
        book.setShelfLocation(blankToNull(form.getShelfLocation()));
        book.setAuthors(new LinkedHashSet<>(authorService.findAllByIds(form.getAuthorIds())));
    }

    private void validateAvailable(BookForm form, BindingResult bindingResult) {
        if (form.getQuantity() != null && form.getAvailable() != null && form.getAvailable() > form.getQuantity()) {
            bindingResult.rejectValue("available", "invalid", "SÃ¡Â»â€˜ lÃ†Â°Ã¡Â»Â£ng cÃƒÂ³ sÃ¡ÂºÂµn khÃƒÂ´ng thÃ¡Â»Æ’ lÃ¡Â»â€ºn hÃ†Â¡n tÃ¡Â»â€¢ng sÃ¡Â»â€˜ lÃ†Â°Ã¡Â»Â£ng.");
        }
    }

    private String blankToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
