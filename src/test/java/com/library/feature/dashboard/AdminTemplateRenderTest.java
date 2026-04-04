package com.library.feature.dashboard;

import com.library.domain.model.Borrow;
import com.library.domain.model.Fine;
import com.library.domain.model.Student;
import com.library.feature.order.OrderItemView;
import com.library.feature.order.OrderRowView;
import com.library.support.ThymeleafRenderSupport;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AdminTemplateRenderTest {

    @Test
    void dashboard_shouldRenderNonEmptyTotals() {
        Map<String, Object> model = new HashMap<>();
        model.put("totalBooks", 120L);
        model.put("totalAvailable", 84L);
        model.put("totalStudents", 42L);
        model.put("activeBorrows", 14L);
        model.put("overdueBorrows", 2L);
        model.put("unpaidFines", 5L);
        model.put("unpaidFineTotal", new BigDecimal("2500000"));
        model.put("totalRevenue", new BigDecimal("5800000"));
        model.put("monthlyBorrowJson", "{\"1\":4}");
        model.put("categoryDistJson", "{\"Kỹ năng\":10}");
        model.put("topBooks", List.of(new TopBorrowedBookView(1, "Dune", 19)));
        model.put("topBorrowers", List.of(new TopBorrowerView(2, "Nguyễn An", 12)));

        String html = ThymeleafRenderSupport.render(
                "admin/dashboard",
                "/admin/dashboard",
                model,
                "admin01",
                "ROLE_ADMIN"
        );

        assertThat(html)
                .contains("2,500,000")
                .contains("5,800,000")
                .contains("#1")
                .contains("Dune");
    }

    @Test
    void fineList_shouldRenderAmountsAndPayAction() {
        Student student = new Student();
        student.setStudentId(9);
        student.setStudentName("Lê Huy");

        Borrow borrow = new Borrow();
        borrow.setBorrowId(73);
        borrow.setStudent(student);

        Fine fine = new Fine();
        fine.setFineId(41);
        fine.setBorrow(borrow);
        fine.setAmount(new BigDecimal("45000"));
        fine.setReason("Trả trễ");
        fine.setCreatedDate(LocalDate.of(2026, 4, 2));
        fine.setStatus("Unpaid");

        Map<String, Object> model = new HashMap<>();
        model.put("fines", new PageImpl<>(List.of(fine), PageRequest.of(0, 15), 16));
        model.put("currentPage", 0);
        model.put("totalPages", 2);
        model.put("statusFilter", null);
        model.put("unpaidCount", 1L);
        model.put("unpaidTotal", new BigDecimal("45000"));
        model.put("paidTotal", new BigDecimal("90000"));

        String html = ThymeleafRenderSupport.render(
                "admin/fine/list",
                "/admin/fines",
                model,
                "admin01",
                "ROLE_ADMIN"
        );

        assertThat(html)
                .contains("45,000")
                .contains("90,000")
                .contains("/libraryManager/admin/fines/41/pay")
                .contains("Trang 1 / 2");
    }

    @Test
    void orderList_shouldRenderOrderTotalsAndItems() {
        OrderRowView order = new OrderRowView(
                15,
                "Trần Hà",
                "Thủ thư B",
                LocalDate.of(2026, 4, 4),
                new BigDecimal("199000"),
                "Đã giao",
                List.of(new OrderItemView(
                        22,
                        "Atomic Habits",
                        1,
                        new BigDecimal("199000"),
                        new BigDecimal("199000")
                ))
        );

        Map<String, Object> model = new HashMap<>();
        model.put("orders", List.of(order));
        model.put("currentPage", 0);
        model.put("totalPages", 1);
        model.put("searchOrderId", 15);
        model.put("searchOrder", order);

        String html = ThymeleafRenderSupport.render(
                "admin/order/list",
                "/admin/orders",
                model,
                "admin01",
                "ROLE_ADMIN"
        );

        assertThat(html)
                .contains("Đơn #15")
                .contains("199,000.00 VND")
                .contains("Atomic Habits");
    }
}
