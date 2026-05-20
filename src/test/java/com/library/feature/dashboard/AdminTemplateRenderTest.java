package com.library.feature.dashboard;

import com.library.domain.model.Book;
import com.library.domain.model.Borrow;
import com.library.domain.model.Fine;
import com.library.domain.model.Publisher;
import com.library.domain.model.Staff;
import com.library.domain.model.Student;
import com.library.feature.catalog.BookForm;
import com.library.feature.catalog.PublisherForm;
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
        model.put("paidFineTotal", new BigDecimal("700000"));
        model.put("totalRevenue", new BigDecimal("5800000"));
        model.put("combinedRevenue", new BigDecimal("6500000"));
        model.put("monthlyBorrowJson", "{\"1\":4}");
        model.put("categoryDistJson", "{\"Ky nang\":10}");
        model.put("revenueChartJson", "{\"labels\":[\"T3/26\",\"T4/26\"],\"orderRevenue\":[120000,140000],\"fineRevenue\":[10000,20000],\"totalRevenue\":[130000,160000]}");
        model.put("topBooks", List.of(new TopBorrowedBookView(1, "Dune", 19)));
        model.put("topBorrowers", List.of(new TopBorrowerView(2, "Nguyen An", 12)));

        String html = ThymeleafRenderSupport.render(
                "admin/dashboard",
                "/admin/dashboard",
                model,
                "admin01",
                "ROLE_ADMIN"
        );

        assertThat(html)
                .contains("/libraryManager/css/style.css")
                .contains("/libraryManager/css/parts/admin.css")
                .contains("/libraryManager/js/app.js")
                .contains("/libraryManager/js/pages/admin-live.js")
                .contains("data-pending-count")
                .contains("/libraryManager/api/pending-count")
                .contains("data-admin-live-url=\"/libraryManager/ws/admin/live\"")
                .contains("data-admin-live-feed")
                .contains("data-admin-live-connection")
                .contains("/libraryManager/js/pages/admin-dashboard.js")
                .contains("id=\"dashboardRevenueData\"")
                .contains("2,500,000")
                .contains("5,800,000")
                .contains("6,500,000")
                .contains("doanh thu 12 th")
                .contains("Bảng quản trị")
                .contains("Quản trị viên")
                .contains("data-sidebar-storage-key=\"libraryManager.adminSidebarCollapsed\"")
                .contains("data-sidebar-backdrop")
                .contains("data-sidebar-scroll")
                .contains("aria-controls=\"sidebar\"")
                .contains("#1")
                .contains("Dune")
                .doesNotContain("Admin Console")
                .doesNotContain("Administrator")
                .doesNotContain("Staff console")
                .doesNotContain("Ã")
                .doesNotContain("Â")
                .doesNotContain("/libraryManager/css/parts/student.css");
    }

    @Test
    void bookCreate_shouldRenderOpenLibraryCoverPicker() {
        Map<String, Object> model = new HashMap<>();
        model.put("form", new BookForm());
        model.put("book", null);
        model.put("categories", List.of());
        model.put("publishers", List.of());
        model.put("authors", List.of());

        String html = ThymeleafRenderSupport.render(
                "admin/book/create",
                "/admin/books/create",
                model,
                "admin01",
                "ROLE_ADMIN"
        );

        assertThat(html)
                .contains("data-cover-picker")
                .contains("data-cover-suggestions-url=\"/libraryManager/admin/books/cover-suggestions\"")
                .contains("data-cover-title-input")
                .contains("data-cover-url-input")
                .contains("data-cover-preview")
                .contains("data-cover-suggestions")
                .contains("Tìm bìa")
                .contains("/libraryManager/js/pages/admin-book-cover.js")
                .doesNotContain("Ãƒ")
                .doesNotContain("Ã‚");
    }

    @Test
    void bookEdit_shouldRenderCurrentCoverPreviewAndPickerScript() {
        Book book = new Book();
        book.setBookId(12);
        book.setBookName("Dune");

        BookForm form = new BookForm();
        form.setBookName("Dune");
        form.setImageUrl("https://covers.openlibrary.org/b/id/8100927-L.jpg?default=false");

        Map<String, Object> model = new HashMap<>();
        model.put("form", form);
        model.put("book", book);
        model.put("categories", List.of());
        model.put("publishers", List.of());
        model.put("authors", List.of());

        String html = ThymeleafRenderSupport.render(
                "admin/book/edit",
                "/admin/books/edit/12",
                model,
                "admin01",
                "ROLE_ADMIN"
        );

        assertThat(html)
                .contains("data-cover-picker")
                .contains("https://covers.openlibrary.org/b/id/8100927-L.jpg?default=false")
                .contains("data-image-fallback=\"https://placehold.co/240x320/e2f3f2/0f3d3b?text=Book\"")
                .contains("/libraryManager/js/pages/admin-book-cover.js")
                .doesNotContain("Ãƒ")
                .doesNotContain("Ã‚");
    }

    @Test
    void fineList_shouldRenderAmountsAndPayAction() {
        Student student = new Student();
        student.setStudentId(9);
        student.setStudentName("Le Huy");

        Borrow borrow = new Borrow();
        borrow.setBorrowId(73);
        borrow.setStudent(student);

        Fine fine = new Fine();
        fine.setFineId(41);
        fine.setBorrow(borrow);
        fine.setAmount(new BigDecimal("45000"));
        fine.setReason("Tra tre");
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
                .contains("Chưa thanh toán")
                .contains("/libraryManager/admin/fines/41/pay")
                .contains("Trang 1 / 2")
                .doesNotContain("Ã")
                .doesNotContain("Â");
    }

    @Test
    void orderList_shouldRenderOrderTotalsAndItems() {
        OrderRowView order = new OrderRowView(
                15,
                "Tran Ha",
                "Thu thu B",
                LocalDate.of(2026, 4, 4),
                new BigDecimal("199000"),
                "Pending",
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
                .contains("Atomic Habits")
                .contains("Đang chờ xử lý")
                .contains("Duyệt đơn chờ")
                .doesNotContain("Ã")
                .doesNotContain("Â");
    }

    @Test
    void borrowList_shouldRenderLocalizedBorrowStatus() {
        Student student = new Student();
        student.setStudentId(9);
        student.setStudentName("Lê Huy");

        Staff staff = new Staff();
        staff.setStaffId(4);
        staff.setStaffName("Thủ thư B");

        Borrow borrow = new Borrow();
        borrow.setBorrowId(73);
        borrow.setStudent(student);
        borrow.setStaff(staff);
        borrow.setBorrowDate(LocalDate.of(2026, 4, 2));
        borrow.setDueDate(LocalDate.of(2026, 4, 16));
        borrow.setStatus("Pending");

        Map<String, Object> model = new HashMap<>();
        model.put("borrows", List.of(borrow));
        model.put("currentPage", 0);
        model.put("totalPages", 1);
        model.put("filterStatus", "Pending");

        String html = ThymeleafRenderSupport.render(
                "admin/borrow/list",
                "/admin/borrows",
                model,
                "admin01",
                "ROLE_ADMIN"
        );

        assertThat(html)
                .contains("Chờ duyệt")
                .contains("Duyệt")
                .contains("Từ chối")
                .contains("Tất cả")
                .doesNotContain("Ã")
                .doesNotContain("Â");
    }

    @Test
    void publisherList_shouldRenderCrudShellWithSidebarHooks() {
        Publisher publisher = new Publisher();
        publisher.setPublisherId(12);
        publisher.setPublisherName("NXB Trẻ");

        Map<String, Object> model = new HashMap<>();
        model.put("form", new PublisherForm());
        model.put("publishers", List.of(publisher));

        String html = ThymeleafRenderSupport.render(
                "admin/publisher/list",
                "/admin/publishers",
                model,
                "admin01",
                "ROLE_ADMIN"
        );

        assertThat(html)
                .contains("admin-crud-grid")
                .contains("crud-form-panel")
                .contains("crud-list-panel")
                .contains("crud-list-stack")
                .contains("crud-inline-card")
                .contains("crud-inline-form")
                .contains("Nhà xuất bản hiện có")
                .contains("data-sidebar-scroll")
                .contains("title=\"Nhà xuất bản\"")
                .doesNotContain("Ã")
                .doesNotContain("Â");
    }
}
