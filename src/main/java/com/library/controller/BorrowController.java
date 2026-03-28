package com.library.controller;

import com.library.entity.Borrow;
import com.library.entity.BorrowItem;
import com.library.entity.Staff;
import com.library.service.BookService;
import com.library.service.BorrowService;
import com.library.service.StaffService;
import com.library.service.StudentService;
import com.library.web.form.BorrowRequestForm;
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

    private final BorrowService borrowService;
    private final BookService bookService;
    private final StudentService studentService;
    private final StaffService staffService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) String status,
                       Model model) {
        Page<Borrow> borrowPage = (status != null && !status.isBlank())
                ? borrowService.findByStatus(status, page, 10)
                : borrowService.findAll(page, 10);

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
        List<BorrowItem> items = buildItems(form);
        borrowService.createBorrow(form.getStudentId(), staff.getStaffId(), items, form.getDueDate());
        redirectAttributes.addFlashAttribute("msg", "Tạo đơn mượn thành công.");
        return "redirect:/admin/borrows";
    }

    @PostMapping("/return/{id}")
    public String returnBorrow(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        borrowService.returnBorrow(id);
        redirectAttributes.addFlashAttribute("msg", "Đã xác nhận trả sách.");
        return "redirect:/admin/borrows";
    }

    @PostMapping("/approve/{id}")
    public String approve(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        borrowService.approveBorrow(id);
        redirectAttributes.addFlashAttribute("msg", "Đã duyệt yêu cầu mượn.");
        return "redirect:/admin/borrows";
    }

    @PostMapping("/reject/{id}")
    public String reject(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        borrowService.rejectBorrow(id);
        redirectAttributes.addFlashAttribute("msg", "Đã từ chối yêu cầu mượn.");
        return "redirect:/admin/borrows";
    }

    private void prepareCreateForm(Model model, BorrowRequestForm form) {
        model.addAttribute("form", form);
        model.addAttribute("students", studentService.findAll());
        model.addAttribute("books", bookService.findAll());
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
