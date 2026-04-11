package com.library.feature.borrow;

import com.library.domain.model.Borrow;
import com.library.domain.model.BorrowItem;
import com.library.domain.model.Staff;
import com.library.feature.catalog.BookService;
import com.library.feature.notification.NotificationService;
import com.library.feature.staff.StaffService;
import com.library.feature.student.StudentService;
import com.library.shared.constant.NotificationType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin/borrows")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowQueryService borrowQueryService;
    private final BorrowRequestService borrowRequestService;
    private final BorrowLifecycleService borrowLifecycleService;
    private final BookService bookService;
    private final StudentService studentService;
    private final StaffService staffService;
    private final NotificationService notificationService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) String status,
                       Model model) {
        Page<Borrow> borrowPage = (status != null && !status.isBlank())
                ? borrowQueryService.findByStatus(status, page, 10)
                : borrowQueryService.findAll(page, 10);

        model.addAttribute("borrows", borrowPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", borrowPage.getTotalPages());
        model.addAttribute("totalItems", borrowPage.getTotalElements());
        model.addAttribute("filterStatus", status);
        return "admin/borrow/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        prepareCreateForm(model, new BorrowRequestForm());
        return "admin/borrow/create";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("form") BorrowRequestForm form,
                         BindingResult bindingResult,
                         Authentication authentication,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (form.getBookIds() == null || form.getBookIds().isEmpty()) {
            bindingResult.reject("bookIds", "Vui lòng chọn ít nhất một sách.");
        }
        if (bindingResult.hasErrors()) {
            prepareCreateForm(model, form);
            return "admin/borrow/create";
        }

        Staff staff = staffService.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên đăng nhập."));
        borrowRequestService.createBorrow(form.getStudentId(), staff.getStaffId(), buildItems(form), form.getDueDate());
        redirectAttributes.addFlashAttribute("msg", "Tạo đơn mượn thành công.");
        return "redirect:/admin/borrows";
    }

    @PostMapping("/return/{id}")
    public String returnBorrow(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        Borrow borrow = borrowLifecycleService.returnBorrow(id);
        if (borrow.getStudent() != null) {
            notificationService.create(borrow.getStudent().getStudentId(),
                    "Sách đã được trả",
                    "Đơn mượn #" + id + " đã được xác nhận trả thành công.",
                    NotificationType.BORROW_RETURNED);
        }
        redirectAttributes.addFlashAttribute("msg", "Đã xác nhận trả sách.");
        return "redirect:/admin/borrows";
    }

    @PostMapping("/approve/{id}")
    public String approve(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        Borrow borrow = borrowLifecycleService.approveBorrow(id);
        if (borrow.getStudent() != null) {
            notificationService.create(borrow.getStudent().getStudentId(),
                    "Yêu cầu mượn được duyệt",
                    "Đơn mượn #" + id + " đã được duyệt. Vui lòng đến thư viện để nhận sách.",
                    NotificationType.BORROW_APPROVED);
        }
        redirectAttributes.addFlashAttribute("msg", "Đã duyệt yêu cầu mượn.");
        return "redirect:/admin/borrows";
    }

    @PostMapping("/reject/{id}")
    public String reject(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        Borrow borrow = borrowLifecycleService.rejectBorrow(id);
        if (borrow.getStudent() != null) {
            notificationService.create(borrow.getStudent().getStudentId(),
                    "Yêu cầu mượn bị từ chối",
                    "Đơn mượn #" + id + " đã bị từ chối.",
                    NotificationType.BORROW_REJECTED);
        }
        redirectAttributes.addFlashAttribute("msg", "Đã từ chối yêu cầu mượn.");
        return "redirect:/admin/borrows";
    }

    private void prepareCreateForm(Model model, BorrowRequestForm form) {
        model.addAttribute("form", form);
        model.addAttribute("students", studentService.findAllOptions());
        model.addAttribute("books", bookService.findAllBorrowOptions());
    }

    private List<BorrowItem> buildItems(BorrowRequestForm form) {
        List<BorrowItem> items = new ArrayList<>();
        List<Integer> quantities = form.getQuantities() == null ? List.of() : form.getQuantities();

        for (int i = 0; i < form.getBookIds().size(); i++) {
            BorrowItem item = new BorrowItem();
            item.setBookId(form.getBookIds().get(i));
            item.setQuantity(i < quantities.size() && quantities.get(i) != null ? quantities.get(i) : 1);
            items.add(item);
        }
        return items;
    }
}
