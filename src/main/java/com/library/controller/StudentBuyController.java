package com.library.controller;

import com.library.entity.Student;
import com.library.service.OrderService;
import com.library.service.StudentContextService;
import com.library.service.StudentSessionService;
import com.library.web.view.PriceDisplayView;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class StudentBuyController {

    private final OrderService orderService;
    private final StudentContextService studentContextService;
    private final StudentSessionService studentSessionService;

    @GetMapping("/buy")
    public String buyCenter(Authentication authentication, HttpSession session, Model model) {
        Student student = studentContextService.resolveCurrentStudent(authentication)
                .orElseThrow(() -> new IllegalArgumentException("Không thể xác định sinh viên hiện tại."));

        model.addAttribute("bookPrices", orderService.getSellableBooks());
        model.addAttribute("waitlistItems", buildWaitlistViews(studentSessionService.waitlist(session)));
        model.addAttribute("orderHistory", orderService.findByStudent(student.getStudentId()));
        return "student/buy";
    }

    @PostMapping("/buy/waitlist/add")
    public String addToWaitlist(@RequestParam Integer bookId,
                                @RequestParam(defaultValue = "1") Integer quantity,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Map<Integer, Integer> waitlist = studentSessionService.waitlist(session);
        waitlist.put(bookId, waitlist.getOrDefault(bookId, 0) + Math.max(1, quantity));
        redirectAttributes.addFlashAttribute("msg", "Đã thêm sách vào danh sách chờ mua.");
        return "redirect:/buy";
    }

    @PostMapping("/buy/waitlist/update/{bookId}")
    public String updateWaitlist(@PathVariable Integer bookId,
                                 @RequestParam Integer quantity,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0.");
        }
        studentSessionService.waitlist(session).put(bookId, quantity);
        redirectAttributes.addFlashAttribute("msg", "Đã cập nhật số lượng mua.");
        return "redirect:/buy";
    }

    @PostMapping("/buy/waitlist/remove/{bookId}")
    public String removeWaitlist(@PathVariable Integer bookId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        studentSessionService.waitlist(session).remove(bookId);
        redirectAttributes.addFlashAttribute("msg", "Đã xóa sách khỏi danh sách chờ mua.");
        return "redirect:/buy";
    }

    @PostMapping("/buy/checkout")
    public String checkout(@RequestParam("selectedBookIds") List<Integer> selectedBookIds,
                           Authentication authentication,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        if (selectedBookIds == null || selectedBookIds.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn ít nhất một sách để đặt.");
        }

        Student student = studentContextService.resolveCurrentStudent(authentication)
                .orElseThrow(() -> new IllegalArgumentException("Không thể xác định sinh viên hiện tại."));
        Integer staffId = studentContextService.findCurrentStaff(authentication)
                .map(staff -> staff.getStaffId())
                .orElseThrow(() -> new IllegalArgumentException("Không thể xác định tài khoản đăng nhập."));

        Map<Integer, Integer> currentWaitlist = studentSessionService.waitlist(session);
        Map<Integer, Integer> orderItems = new LinkedHashMap<>();
        for (Integer bookId : selectedBookIds) {
            Integer quantity = currentWaitlist.get(bookId);
            if (quantity != null && quantity > 0) {
                orderItems.put(bookId, quantity);
            }
        }
        if (orderItems.isEmpty()) {
            throw new IllegalArgumentException("Không có sách hợp lệ để đặt.");
        }

        orderService.createStudentOrder(student.getStudentId(), staffId, orderItems);
        orderItems.keySet().forEach(currentWaitlist::remove);
        redirectAttributes.addFlashAttribute("msg", "Đã tạo đơn mua sách thành công.");
        return "redirect:/buy";
    }

    private List<Map<String, Object>> buildWaitlistViews(Map<Integer, Integer> waitlist) {
        Map<Integer, PriceDisplayView> priceMap = orderService.getSellableBooks().stream()
                .collect(java.util.stream.Collectors.toMap(PriceDisplayView::bookId, item -> item));

        return waitlist.entrySet().stream()
                .map(entry -> {
                    PriceDisplayView price = priceMap.get(entry.getKey());
                    if (price == null) {
                        return null;
                    }
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("bookId", price.bookId());
                    row.put("bookName", price.bookName());
                    row.put("quantity", entry.getValue());
                    row.put("amount", price.amount());
                    row.put("lineTotal", price.amount().multiply(java.math.BigDecimal.valueOf(entry.getValue())));
                    return row;
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }
}
