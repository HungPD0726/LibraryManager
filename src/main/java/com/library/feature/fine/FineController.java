package com.library.feature.fine;

import com.library.domain.model.Borrow;
import com.library.domain.model.Fine;
import com.library.feature.borrow.BorrowQueryService;
import com.library.feature.notification.NotificationService;
import com.library.shared.constant.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/admin/fines")
@RequiredArgsConstructor
public class FineController {

    private final FineService fineService;
    private final BorrowQueryService borrowQueryService;
    private final NotificationService notificationService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) String status,
                       Model model) {
        Page<Fine> finePage = (status != null && !status.isBlank())
                ? fineService.findByStatus(status, page, 15)
                : fineService.findAll(page, 15);

        model.addAttribute("fines", finePage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", finePage.getTotalPages());
        model.addAttribute("statusFilter", status);
        model.addAttribute("unpaidCount", fineService.countUnpaid());
        model.addAttribute("unpaidTotal", fineService.sumUnpaidAmount());
        model.addAttribute("paidTotal", fineService.sumPaidAmount());
        return "admin/fine/list";
    }

    @GetMapping("/create")
    public String createForm(@RequestParam(required = false) Integer borrowId, Model model) {
        model.addAttribute("borrowId", borrowId);
        if (borrowId != null) {
            borrowQueryService.findById(borrowId).ifPresent(borrow -> model.addAttribute("borrow", borrow));
        }
        return "admin/fine/create";
    }

    @PostMapping("/create")
    public String create(@RequestParam Integer borrowId,
                         @RequestParam BigDecimal amount,
                         @RequestParam(required = false) String reason,
                         RedirectAttributes redirectAttributes) {
        Fine fine = fineService.createFine(borrowId, amount, reason);
        Borrow borrow = fine.getBorrow();
        if (borrow.getStudent() != null) {
            notificationService.create(
                    borrow.getStudent().getStudentId(),
                    "Phạt mới được tạo",
                    "Bạn có phiếu phạt mới: " + amount + " VND. Lý do: " + (reason != null ? reason : "Trả sách trễ"),
                    NotificationType.FINE_CREATED
            );
        }

        redirectAttributes.addFlashAttribute("msg", "Đã tạo phiếu phạt thành công.");
        return "redirect:/admin/fines";
    }

    @PostMapping("/{id}/pay")
    public String markPaid(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        Fine fine = fineService.markPaid(id);
        if (fine.getBorrow() != null && fine.getBorrow().getStudent() != null) {
            notificationService.create(
                    fine.getBorrow().getStudent().getStudentId(),
                    "Phạt đã thanh toán",
                    "Phiếu phạt #" + id + " đã được xác nhận thanh toán.",
                    NotificationType.FINE_PAID
            );
        }

        redirectAttributes.addFlashAttribute("msg", "Đã xác nhận thanh toán phiếu phạt #" + id + ".");
        return "redirect:/admin/fines";
    }

    @PostMapping("/auto-generate")
    public String autoGenerate(RedirectAttributes redirectAttributes) {
        int count = fineService.autoGenerateOverdueFines();
        redirectAttributes.addFlashAttribute("msg", "Đã tạo " + count + " phiếu phạt tự động cho đơn quá hạn.");
        return "redirect:/admin/fines";
    }
}
