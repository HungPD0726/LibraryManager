package com.group6.librarymanager.controller;

import com.group6.librarymanager.model.dao.*;
import com.group6.librarymanager.model.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderApiController {

    @Autowired
    private OrdersDAO ordersDAO;

    @Autowired
    private OrderDetailDAO orderDetailDAO;

    @Autowired
    private StudentDAO studentDAO;

    @Autowired
    private StaffDAO staffDAO;

    @Autowired
    private BookDAO bookDAO;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllOrders() {
        List<Orders> orders = ordersDAO.findAll();
        List<Map<String, Object>> result = orders.stream().map(this::toOrderResponse).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Map<String, Object> request) {
        try {
            Orders order = new Orders();

            Integer studentId = (Integer) request.get("studentId");
            Student student = studentDAO.findById(studentId)
                    .orElseThrow(() -> new RuntimeException("Student not found"));
            order.setStudent(student);

            Integer staffId = (Integer) request.get("staffId");
            Staff staff = staffDAO.findById(staffId)
                    .orElseThrow(() -> new RuntimeException("Staff not found"));
            order.setStaff(staff);

            order.setOrderDate(LocalDate.now());
            order.setStatus("Completed");

            // Calculate total from items
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) request.get("items");
            BigDecimal total = BigDecimal.ZERO;

            Orders savedOrder = ordersDAO.save(order);

            if (items != null) {
                for (Map<String, Object> item : items) {
                    Integer bookId = (Integer) item.get("bookId");
                    Integer qty = (Integer) item.getOrDefault("quantity", 1);
                    BigDecimal unitPrice = new BigDecimal(item.get("unitPrice").toString());

                    OrderDetail detail = new OrderDetail();
                    detail.setOrderId(savedOrder.getOrderId());
                    detail.setBookId(bookId);
                    detail.setQuantity(qty);
                    detail.setUnitPrice(unitPrice);
                    orderDetailDAO.save(detail);

                    total = total.add(unitPrice.multiply(BigDecimal.valueOf(qty)));

                    // Update book availability
                    bookDAO.findById(bookId).ifPresent(book -> {
                        book.setAvailable(book.getAvailable() - qty);
                        book.setQuantity(book.getQuantity() - qty);
                        bookDAO.save(book);
                    });
                }
            }

            savedOrder.setTotalAmount(total);
            ordersDAO.save(savedOrder);

            Orders reloaded = ordersDAO.findById(savedOrder.getOrderId()).orElse(savedOrder);
            return ResponseEntity.ok(toOrderResponse(reloaded));
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    private Map<String, Object> toOrderResponse(Orders order) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("orderId", order.getOrderId());
        map.put("studentId", order.getStudent() != null ? order.getStudent().getStudentId() : null);
        map.put("studentName", order.getStudent() != null ? order.getStudent().getStudentName() : null);
        map.put("staffId", order.getStaff() != null ? order.getStaff().getStaffId() : null);
        map.put("orderDate", order.getOrderDate() != null ? order.getOrderDate().toString() : null);
        map.put("totalAmount", order.getTotalAmount());
        map.put("status", order.getStatus());

        if (order.getOrderDetails() != null) {
            List<Map<String, Object>> details = order.getOrderDetails().stream().map(d -> {
                Map<String, Object> dm = new LinkedHashMap<>();
                dm.put("bookId", d.getBookId());
                dm.put("bookName", d.getBook() != null ? d.getBook().getBookName() : null);
                dm.put("quantity", d.getQuantity());
                dm.put("unitPrice", d.getUnitPrice());
                dm.put("total", d.getUnitPrice().multiply(BigDecimal.valueOf(d.getQuantity())));
                return dm;
            }).collect(Collectors.toList());
            map.put("items", details);
        }

        return map;
    }
}
