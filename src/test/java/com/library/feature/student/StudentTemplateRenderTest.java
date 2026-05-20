package com.library.feature.student;

import com.library.domain.model.BookReview;
import com.library.domain.model.Borrow;
import com.library.domain.model.Notification;
import com.library.domain.model.Student;
import com.library.feature.borrow.HoldRowView;
import com.library.feature.catalog.BookCardView;
import com.library.feature.catalog.BookDetailView;
import com.library.feature.catalog.BookFileView;
import com.library.feature.catalog.PriceDisplayView;
import com.library.feature.order.OrderItemView;
import com.library.feature.order.OrderRowView;
import com.library.feature.order.WaitlistItemView;
import com.library.support.ThymeleafRenderSupport;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class StudentTemplateRenderTest {

    @Test
    void home_shouldRenderBookShelfWithPrice() {
        Student student = student();

        Map<String, Object> model = new HashMap<>();
        model.put("currentStudent", student);
        model.put("studentDisplayName", student.getStudentName());
        model.put("summary", new StudentHomeSummary(1, 2, 3, 0, 1, 0));
        model.put("books", List.of(new BookCardView(
                15,
                "Sapiens",
                "Lịch sử",
                "NXB Trẻ",
                null,
                4,
                new BigDecimal("120000"),
                "VND",
                List.of("Yuval Noah Harari")
        )));
        model.put("categories", List.of());
        model.put("publishers", List.of());
        model.put("currentPage", 0);
        model.put("totalPages", 1);
        model.put("search", null);
        model.put("letter", null);
        model.put("categoryId", null);
        model.put("publisherId", null);
        model.put("author", null);

        String html = ThymeleafRenderSupport.render(
                "student/home",
                "/home",
                model,
                "student01",
                "ROLE_STUDENT"
        );

        assertThat(html)
                .contains("/libraryManager/css/style.css")
                .contains("/libraryManager/css/parts/student.css")
                .contains("/libraryManager/js/app.js")
                .contains("/libraryManager/uploads/avatars/minh.png")
                .contains("Sapiens")
                .contains("data-image-fallback=\"https://placehold.co/320x420/e3f7f5/0f3d3b?text=Library\"")
                .contains("loading=\"lazy\"")
                .contains("120,000.00 VND")
                .contains("Sách có thể mượn hoặc đặt giữ chỗ ngay")
                .contains("Cổng sinh viên")
                .contains("Trợ lý thư viện")
                .doesNotContain("Student Portal")
                .doesNotContain("Chatbot")
                .doesNotContain("Assistant")
                .doesNotContain("Ã")
                .doesNotContain("Â")
                .doesNotContain("/libraryManager/css/parts/admin.css");
    }

    @Test
    void buy_shouldRenderWaitlistAndOrderHistory() {
        Map<String, Object> model = new HashMap<>();
        model.put("bookPrices", List.of(new PriceDisplayView(
                11,
                "Doraemon",
                8,
                new BigDecimal("45000"),
                "VND",
                null
        )));
        model.put("waitlistItems", List.of(new WaitlistItemView(
                11,
                "Doraemon",
                2,
                new BigDecimal("45000"),
                new BigDecimal("90000")
        )));
        model.put("orderHistory", List.of(new OrderRowView(
                31,
                "Nguyễn Minh",
                "Thủ thư A",
                LocalDate.of(2026, 4, 4),
                new BigDecimal("90000"),
                "Pending",
                List.of(new OrderItemView(
                        11,
                        "Doraemon",
                        2,
                        new BigDecimal("45000"),
                        new BigDecimal("90000")
                ))
        )));

        String html = ThymeleafRenderSupport.render(
                "student/buy",
                "/buy",
                model,
                "student01",
                "ROLE_STUDENT"
        );

        assertThat(html)
                .contains("Doraemon")
                .contains("45,000.00 VND")
                .contains("90,000.00 VND")
                .contains("Đơn #31")
                .contains("Đang chờ xử lý")
                .doesNotContain("Pending")
                .doesNotContain("Ã")
                .doesNotContain("Â");
    }

    @Test
    void borrows_shouldRenderLocalizedHoldAndBorrowStatuses() {
        Student student = student();

        Borrow borrow = new Borrow();
        borrow.setBorrowId(19);
        borrow.setBorrowDate(LocalDate.of(2026, 4, 1));
        borrow.setDueDate(LocalDate.of(2026, 4, 15));
        borrow.setStatus("Pending");

        Map<String, Object> model = new HashMap<>();
        model.put("currentStudent", student);
        model.put("cartItems", List.of());
        model.put("activeHolds", List.of(new HoldRowView(
                5,
                11,
                "Doraemon",
                "Waiting",
                LocalDateTime.of(2026, 4, 2, 9, 0),
                null,
                "Giữ giúp tôi"
        )));
        model.put("borrowHistory", List.of(borrow));

        String html = ThymeleafRenderSupport.render(
                "student/borrows",
                "/borrows",
                model,
                "student01",
                "ROLE_STUDENT"
        );

        assertThat(html)
                .contains("Đang chờ")
                .contains("Chờ duyệt")
                .contains("Hủy giữ chỗ")
                .doesNotContain("Waiting")
                .doesNotContain("Pending")
                .doesNotContain("Ã")
                .doesNotContain("Â");
    }

    @Test
    void notifications_shouldRenderUnreadBadgeAndAction() {
        Notification notification = new Notification();
        notification.setNotificationId(99);
        notification.setStudent(student());
        notification.setTitle("Sách đã sẵn sàng");
        notification.setMessage("Bạn có thể quay lại thư viện để nhận sách.");
        notification.setType("ORDER_DELIVERED");
        notification.setIsRead(false);
        notification.setCreatedDate(LocalDateTime.of(2026, 4, 4, 9, 30));

        Map<String, Object> model = Map.of(
                "notifications", List.of(notification),
                "unreadCount", 1L
        );

        String html = ThymeleafRenderSupport.render(
                "student/notifications",
                "/notifications",
                model,
                "student01",
                "ROLE_STUDENT"
        );

        assertThat(html)
                .contains("1 chưa đọc")
                .contains("/libraryManager/notifications/99/read")
                .contains("Sách đã sẵn sàng")
                .doesNotContain("Ã")
                .doesNotContain("Â");
    }

    @Test
    void bookDetail_shouldRenderReviewSummary() {
        Student student = student();

        BookReview review = new BookReview();
        review.setReviewId(51);
        review.setStudent(student);
        review.setRating(5);
        review.setComment("Rất đáng đọc");
        review.setCreatedDate(LocalDateTime.of(2026, 4, 1, 8, 0));

        Map<String, Object> model = new HashMap<>();
        model.put("currentStudent", student);
        model.put("bookDetail", new BookDetailView(
                22,
                "Atomic Habits",
                "Self-help",
                "Penguin",
                null,
                12,
                6,
                new BigDecimal("199000"),
                "VND",
                "Bìa mềm",
                "Thay đổi từng thói quen nhỏ.",
                "A-02",
                List.of("James Clear"),
                List.of(new BookFileView(
                        14,
                        22,
                        "Atomic Habits",
                        "Thủ thư A",
                        "atomic-habits.pdf",
                        "https://example.test/atomic-habits.pdf",
                        "PDF",
                        1024L,
                        LocalDateTime.of(2026, 4, 2, 12, 0),
                        true
                ))
        ));
        model.put("reviews", List.of(review));
        model.put("averageRating", 4.5d);
        model.put("reviewCount", 2L);
        model.put("hasReviewed", true);

        String html = ThymeleafRenderSupport.render(
                "student/book-detail",
                "/home/book",
                model,
                "student01",
                "ROLE_STUDENT"
        );

        assertThat(html)
                .contains("Atomic Habits")
                .contains("data-image-fallback=\"https://placehold.co/400x520/e3f7f5/0f3d3b?text=Library\"")
                .contains("199,000.00 VND")
                .contains("2 lượt đánh giá")
                .contains("Rất đáng đọc")
                .doesNotContain("Ã")
                .doesNotContain("Â");
    }

    @Test
    void chatbot_shouldLoadSharedStudentAssetsAndPageScript() {
        Map<String, Object> model = new HashMap<>();
        model.put("chatbotConfigured", true);
        model.put("chatbotModel", "llama-3.1-8b-instant");
        model.put("viewerName", "Nguyen Minh");

        String html = ThymeleafRenderSupport.render(
                "student/chatbot",
                "/chatbot",
                model,
                "student01",
                "ROLE_STUDENT"
        );

        assertThat(html)
                .contains("/libraryManager/css/style.css")
                .contains("/libraryManager/css/parts/student.css")
                .contains("/libraryManager/js/app.js")
                .contains("/libraryManager/js/pages/chatbot.js")
                .contains("Trợ lý thư viện")
                .contains("Mô hình: llama-3.1-8b-instant")
                .doesNotContain("Chatbot")
                .doesNotContain("Assistant")
                .doesNotContain("Library AI")
                .doesNotContain("Student Portal")
                .doesNotContain("Ã")
                .doesNotContain("Â")
                .doesNotContain("/libraryManager/js/pages/admin-dashboard.js");
    }

    private static Student student() {
        Student student = new Student();
        student.setStudentId(7);
        student.setStudentName("Nguyễn Minh");
        student.setEmail("minh@example.com");
        student.setAvatarUrl("/uploads/avatars/minh.png");
        return student;
    }
}
