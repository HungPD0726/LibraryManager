package com.group6.librarymanager.controller;

import com.group6.librarymanager.model.dao.AuthorDAO;
import com.group6.librarymanager.model.dao.BookDAO;
import com.group6.librarymanager.model.dao.CategoryDAO;
import com.group6.librarymanager.model.dao.PublisherDAO;
import com.group6.librarymanager.model.entity.Author;
import com.group6.librarymanager.model.entity.Book;
import com.group6.librarymanager.model.entity.Category;
import com.group6.librarymanager.model.entity.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
public class BookApiController {

    @Autowired
    private BookDAO bookDAO;

    @Autowired
    private CategoryDAO categoryDAO;

    @Autowired
    private PublisherDAO publisherDAO;

    @Autowired
    private AuthorDAO authorDAO;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllBooks() {
        List<Book> books = bookDAO.findAll();
        List<Map<String, Object>> result = books.stream().map(this::toBookResponse).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getBookById(@PathVariable Integer id) {
        return bookDAO.findById(id)
                .map(book -> ResponseEntity.ok(toBookResponse(book)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createBook(@RequestBody Map<String, Object> request) {
        try {
            Book book = new Book();
            book.setBookName((String) request.get("bookName"));
            book.setQuantity((Integer) request.get("quantity"));
            book.setAvailable((Integer) request.getOrDefault("available", request.get("quantity")));

            Integer categoryId = (Integer) request.get("categoryId");
            Category category = categoryDAO.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            book.setCategory(category);

            Integer publisherId = (Integer) request.get("publisherId");
            Publisher publisher = publisherDAO.findById(publisherId)
                    .orElseThrow(() -> new RuntimeException("Publisher not found"));
            book.setPublisher(publisher);

            // Handle authors
            @SuppressWarnings("unchecked")
            List<Integer> authorIds = (List<Integer>) request.get("authorIds");
            if (authorIds != null && !authorIds.isEmpty()) {
                List<Author> authors = authorDAO.findAllById(authorIds);
                book.setAuthors(authors);
            }

            Book savedBook = bookDAO.save(book);
            return ResponseEntity.ok(toBookResponse(savedBook));
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateBook(@PathVariable Integer id,
            @RequestBody Map<String, Object> request) {
        return bookDAO.findById(id).map(book -> {
            if (request.containsKey("bookName"))
                book.setBookName((String) request.get("bookName"));
            if (request.containsKey("quantity"))
                book.setQuantity((Integer) request.get("quantity"));
            if (request.containsKey("available"))
                book.setAvailable((Integer) request.get("available"));

            if (request.containsKey("categoryId")) {
                Integer categoryId = (Integer) request.get("categoryId");
                categoryDAO.findById(categoryId).ifPresent(book::setCategory);
            }

            if (request.containsKey("publisherId")) {
                Integer publisherId = (Integer) request.get("publisherId");
                publisherDAO.findById(publisherId).ifPresent(book::setPublisher);
            }

            Book updated = bookDAO.save(book);
            return ResponseEntity.ok(toBookResponse(updated));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Integer id) {
        if (bookDAO.existsById(id)) {
            bookDAO.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    private Map<String, Object> toBookResponse(Book book) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("bookId", book.getBookId());
        map.put("bookName", book.getBookName());
        map.put("quantity", book.getQuantity());
        map.put("available", book.getAvailable());
        map.put("categoryId", book.getCategory() != null ? book.getCategory().getCategoryId() : null);
        map.put("categoryName", book.getCategory() != null ? book.getCategory().getCategoryName() : null);
        map.put("publisherId", book.getPublisher() != null ? book.getPublisher().getPublisherId() : null);
        map.put("publisherName", book.getPublisher() != null ? book.getPublisher().getPublisherName() : null);

        if (book.getAuthors() != null) {
            List<Map<String, Object>> authors = book.getAuthors().stream().map(a -> {
                Map<String, Object> am = new HashMap<>();
                am.put("authorId", a.getAuthorId());
                am.put("authorName", a.getAuthorName());
                return am;
            }).collect(Collectors.toList());
            map.put("authors", authors);
        } else {
            map.put("authors", List.of());
        }

        // Compute status
        int available = book.getAvailable() != null ? book.getAvailable() : 0;
        int quantity = book.getQuantity() != null ? book.getQuantity() : 0;
        String status;
        if (available == 0)
            status = "out";
        else if (available <= quantity * 0.2)
            status = "low";
        else
            status = "available";
        map.put("status", status);

        return map;
    }
}
