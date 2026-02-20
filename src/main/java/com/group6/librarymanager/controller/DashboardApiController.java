package com.group6.librarymanager.controller;

import com.group6.librarymanager.model.dao.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardApiController {

    @Autowired
    private BookDAO bookDAO;

    @Autowired
    private StudentDAO studentDAO;

    @Autowired
    private BorrowDAO borrowDAO;

    @Autowired
    private OrdersDAO ordersDAO;

    @Autowired
    private CategoryDAO categoryDAO;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        long totalBooks = bookDAO.count();
        long totalStudents = studentDAO.count();
        long activeBorrows = borrowDAO.findByStatus("Borrowing").size();
        long overdueBorrows = borrowDAO.findByStatus("Overdue").size();
        long totalOrders = ordersDAO.count();
        long totalCategories = categoryDAO.count();

        stats.put("totalBooks", totalBooks);
        stats.put("totalStudents", totalStudents);
        stats.put("activeBorrows", activeBorrows);
        stats.put("overdueBorrows", overdueBorrows);
        stats.put("totalOrders", totalOrders);
        stats.put("totalCategories", totalCategories);

        return ResponseEntity.ok(stats);
    }
}
