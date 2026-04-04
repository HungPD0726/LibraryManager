package com.library.feature.order;

import com.library.domain.model.Orders;
import com.library.domain.model.Staff;
import com.library.feature.notification.NotificationService;
import com.library.feature.staff.StaffService;
import com.library.shared.constant.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class OrderAdminController {

    private final OrderQueryService orderQueryService;
    private final OrderLifecycleService orderLifecycleService;
    private final StaffService staffService;
    private final NotificationService notificationService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) Integer orderId,
                       Model model) {
        Page<OrderRowView> orderPage = orderQueryService.findAdminPage(page, 10);
        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("searchOrderId", orderId);
        if (orderId != null) {
            orderQueryService.findOrderView(orderId).ifPresent(view -> model.addAttribute("searchOrder", view));
        }
        return "admin/order/list";
    }

    @PostMapping("/complete/{id}")
    public String complete(@PathVariable Integer id,
                           Authentication authentication,
                           RedirectAttributes redirectAttributes) {
        Staff staff = resolveStaff(authentication);
        Orders order = orderLifecycleService.completeDelivery(id, staff.getStaffId());
        if (order.getStudent() != null) {
            notificationService.create(order.getStudent().getStudentId(),
                    "Đơn mua đã giao",
                    "Đơn mua #" + id + " đã được giao thành công.",
                    NotificationType.ORDER_DELIVERED);
        }
        redirectAttributes.addFlashAttribute("msg", "Đã hoàn tất giao sách cho đơn #" + id + ".");
        return "redirect:/admin/orders";
    }

    @PostMapping("/cancel/{id}")
    public String cancel(@PathVariable Integer id,
                         Authentication authentication,
                         RedirectAttributes redirectAttributes) {
        Staff staff = resolveStaff(authentication);
        Orders order = orderLifecycleService.cancelOrder(id, staff.getStaffId());
        if (order.getStudent() != null) {
            notificationService.create(order.getStudent().getStudentId(),
                    "Đơn mua bị hủy",
                    "Đơn mua #" + id + " đã bị hủy.",
                    NotificationType.ORDER_CANCELLED);
        }
        redirectAttributes.addFlashAttribute("msg", "Đã hủy đơn #" + id + ".");
        return "redirect:/admin/orders";
    }

    @PostMapping("/approve/{id}")
    public String approve(@PathVariable Integer id,
                          Authentication authentication,
                          RedirectAttributes redirectAttributes) {
        Staff staff = resolveStaff(authentication);
        orderLifecycleService.approveLegacyPending(id, staff.getStaffId());
        redirectAttributes.addFlashAttribute("msg", "Đã chuyển đơn Pending sang trạng thái sẵn sàng.");
        return "redirect:/admin/orders";
    }

    @PostMapping("/reject/{id}")
    public String reject(@PathVariable Integer id,
                         Authentication authentication,
                         RedirectAttributes redirectAttributes) {
        Staff staff = resolveStaff(authentication);
        orderLifecycleService.rejectLegacyPending(id, staff.getStaffId());
        redirectAttributes.addFlashAttribute("msg", "Đã từ chối đơn Pending.");
        return "redirect:/admin/orders";
    }

    private Staff resolveStaff(Authentication authentication) {
        return staffService.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên đăng nhập."));
    }
}
