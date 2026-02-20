package com.group6.librarymanager.controller;

import com.group6.librarymanager.model.dao.*;
import com.group6.librarymanager.model.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/borrows")
public class BorrowApiController {

    @Autowired
    private BorrowDAO borrowDAO;

    @Autowired
    private BorrowItemDAO borrowItemDAO;

    @Autowired
    private StudentDAO studentDAO;

    @Autowired
    private StaffDAO staffDAO;

    @Autowired
    private BookDAO bookDAO;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllBorrows() {
        List<Borrow> borrows = borrowDAO.findAll();
        List<Map<String, Object>> result = borrows.stream().map(this::toBorrowResponse).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createBorrow(@RequestBody Map<String, Object> request) {
        try {
            Borrow borrow = new Borrow();

            Integer studentId = (Integer) request.get("studentId");
            Student student = studentDAO.findById(studentId)
                    .orElseThrow(() -> new RuntimeException("Student not found"));
            borrow.setStudent(student);

            Integer staffId = (Integer) request.get("staffId");
            Staff staff = staffDAO.findById(staffId)
                    .orElseThrow(() -> new RuntimeException("Staff not found"));
            borrow.setStaff(staff);

            borrow.setBorrowDate(LocalDate.now());

            String dueDateStr = (String) request.get("dueDate");
            borrow.setDueDate(LocalDate.parse(dueDateStr));
            borrow.setStatus("Borrowing");

            Borrow savedBorrow = borrowDAO.save(borrow);

            // Save borrow items
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) request.get("items");
            if (items != null) {
                for (Map<String, Object> item : items) {
                    Integer bookId = (Integer) item.get("bookId");
                    Integer qty = (Integer) item.getOrDefault("quantity", 1);

                    BorrowItem borrowItem = new BorrowItem();
                    borrowItem.setBorrowId(savedBorrow.getBorrowId());
                    borrowItem.setBookId(bookId);
                    borrowItem.setQuantity(qty);
                    borrowItemDAO.save(borrowItem);

                    // Update book available count
                    bookDAO.findById(bookId).ifPresent(book -> {
                        book.setAvailable(book.getAvailable() - qty);
                        bookDAO.save(book);
                    });
                }
            }

            // Reload to get full data
            Borrow reloaded = borrowDAO.findById(savedBorrow.getBorrowId()).orElse(savedBorrow);
            return ResponseEntity.ok(toBorrowResponse(reloaded));
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/{id}/return")
    public ResponseEntity<Map<String, Object>> returnBorrow(@PathVariable Integer id) {
        return borrowDAO.findById(id).map(borrow -> {
            borrow.setStatus("Returned");
            borrowDAO.save(borrow);

            // Restore book availability
            if (borrow.getBorrowItems() != null) {
                for (BorrowItem item : borrow.getBorrowItems()) {
                    bookDAO.findById(item.getBookId()).ifPresent(book -> {
                        book.setAvailable(book.getAvailable() + item.getQuantity());
                        bookDAO.save(book);
                    });
                }
            }

            return ResponseEntity.ok(toBorrowResponse(borrow));
        }).orElse(ResponseEntity.notFound().build());
    }

    private Map<String, Object> toBorrowResponse(Borrow borrow) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("borrowId", borrow.getBorrowId());
        map.put("studentId", borrow.getStudent() != null ? borrow.getStudent().getStudentId() : null);
        map.put("studentName", borrow.getStudent() != null ? borrow.getStudent().getStudentName() : null);
        map.put("staffId", borrow.getStaff() != null ? borrow.getStaff().getStaffId() : null);
        map.put("staffName", borrow.getStaff() != null ? borrow.getStaff().getStaffName() : null);
        map.put("borrowDate", borrow.getBorrowDate() != null ? borrow.getBorrowDate().toString() : null);
        map.put("dueDate", borrow.getDueDate() != null ? borrow.getDueDate().toString() : null);
        map.put("status", borrow.getStatus());

        if (borrow.getBorrowItems() != null) {
            List<Map<String, Object>> items = borrow.getBorrowItems().stream().map(item -> {
                Map<String, Object> im = new HashMap<>();
                im.put("bookId", item.getBookId());
                im.put("bookName", item.getBook() != null ? item.getBook().getBookName() : null);
                im.put("quantity", item.getQuantity());
                return im;
            }).collect(Collectors.toList());
            map.put("items", items);
        }

        return map;
    }
}
