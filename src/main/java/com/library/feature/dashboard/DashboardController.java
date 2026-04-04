package com.library.feature.dashboard;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardReadService dashboardReadService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        DashboardPageView dashboard = dashboardReadService.read();
        model.addAttribute("totalBooks", dashboard.totalBooks());
        model.addAttribute("totalAvailable", dashboard.totalAvailable());
        model.addAttribute("totalStudents", dashboard.totalStudents());
        model.addAttribute("totalStaff", dashboard.totalStaff());
        model.addAttribute("totalCategories", dashboard.totalCategories());
        model.addAttribute("pendingBorrows", dashboard.pendingBorrows());
        model.addAttribute("activeBorrows", dashboard.activeBorrows());
        model.addAttribute("overdueBorrows", dashboard.overdueBorrows());
        model.addAttribute("unpaidFines", dashboard.unpaidFines());
        model.addAttribute("unpaidFineTotal", dashboard.unpaidFineTotal());
        model.addAttribute("paidFineTotal", dashboard.paidFineTotal());
        model.addAttribute("totalRevenue", dashboard.totalRevenue());
        model.addAttribute("monthlyBorrowJson", dashboard.charts().monthlyBorrowJson());
        model.addAttribute("categoryDistJson", dashboard.charts().categoryDistJson());
        model.addAttribute("borrowStatusJson", dashboard.charts().borrowStatusJson());
        model.addAttribute("topBooks", dashboard.topBooks());
        model.addAttribute("topBorrowers", dashboard.topBorrowers());
        return "admin/dashboard";
    }
}
