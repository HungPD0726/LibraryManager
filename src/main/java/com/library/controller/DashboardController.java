package com.library.controller;

import com.library.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class DashboardController {

    private final BookService bookService;
    private final StudentService studentService;
    private final StaffService staffService;
    private final BorrowService borrowService;
    private final CategoryService categoryService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalBooks", bookService.countAll());
        model.addAttribute("totalAvailable", bookService.countTotalAvailable());
        model.addAttribute("totalStudents", studentService.count());
        model.addAttribute("totalStaff", staffService.count());
        model.addAttribute("totalCategories", categoryService.count());
        model.addAttribute("pendingBorrows", borrowService.countPending());
        model.addAttribute("activeBorrows", borrowService.countBorrowing());
        model.addAttribute("overdueBorrows", borrowService.countOverdue());
        return "admin/dashboard";
    }
}
