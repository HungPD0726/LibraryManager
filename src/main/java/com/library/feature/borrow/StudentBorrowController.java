package com.library.feature.borrow;

import com.library.domain.model.BorrowItem;
import com.library.domain.model.Staff;
import com.library.domain.model.Student;
import com.library.feature.student.CurrentStudentService;
import com.library.feature.student.StudentSessionService;
import com.library.shared.realtime.AdminLiveUpdateService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class StudentBorrowController {

    private final BorrowRequestService borrowRequestService;
    private final StudentBorrowReadService studentBorrowReadService;
    private final BookHoldService bookHoldService;
    private final CurrentStudentService currentStudentService;
    private final StudentSessionService studentSessionService;
    private final AdminLiveUpdateService adminLiveUpdateService;

    @GetMapping("/borrows")
    public String borrowCenter(Authentication authentication, HttpSession session, Model model) {
        Student student = currentStudentService.resolveCurrentStudent(authentication)
                .orElseThrow(() -> new IllegalArgumentException("KhÃ´ng thá»ƒ xÃ¡c Ä‘á»‹nh sinh viÃªn hiá»‡n táº¡i."));

        StudentBorrowPageView view = studentBorrowReadService.buildPage(
                student.getStudentId(),
                studentSessionService.borrowCart(session)
        );
        model.addAttribute("cartItems", view.cartItems());
        model.addAttribute("borrowHistory", view.borrowHistory());
        model.addAttribute("activeHolds", view.activeHolds());
        model.addAttribute("defaultDueDate", view.defaultDueDate());
        return "student/borrows";
    }

    @PostMapping("/borrows/cart/add")
    public String addToCart(@RequestParam Integer bookId,
                            @RequestParam(defaultValue = "1") Integer quantity,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        Map<Integer, Integer> cart = studentSessionService.borrowCart(session);
        cart.put(bookId, cart.getOrDefault(bookId, 0) + Math.max(1, quantity));
        redirectAttributes.addFlashAttribute("msg", "ÄÃ£ thÃªm sÃ¡ch vÃ o giá» mÆ°á»£n.");
        return "redirect:/borrows";
    }

    @PostMapping("/borrows/cart/remove/{bookId}")
    public String removeFromCart(@PathVariable Integer bookId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        studentSessionService.borrowCart(session).remove(bookId);
        redirectAttributes.addFlashAttribute("msg", "ÄÃ£ xÃ³a sÃ¡ch khá»i giá» mÆ°á»£n.");
        return "redirect:/borrows";
    }

    @PostMapping("/borrows/request")
    public String submitBorrow(@RequestParam LocalDate dueDate,
                               Authentication authentication,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Student student = currentStudentService.resolveCurrentStudent(authentication)
                .orElseThrow(() -> new IllegalArgumentException("KhÃ´ng thá»ƒ xÃ¡c Ä‘á»‹nh sinh viÃªn hiá»‡n táº¡i."));
        Staff staff = currentStudentService.findCurrentStaff(authentication)
                .orElseThrow(() -> new IllegalArgumentException("KhÃ´ng thá»ƒ xÃ¡c Ä‘á»‹nh tÃ i khoáº£n Ä‘Äƒng nháº­p."));

        Map<Integer, Integer> cart = studentSessionService.borrowCart(session);
        if (cart.isEmpty()) {
            throw new IllegalArgumentException("Giá» mÆ°á»£n Ä‘ang trá»‘ng.");
        }

        List<BorrowItem> items = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            BorrowItem item = new BorrowItem();
            item.setBookId(entry.getKey());
            item.setQuantity(entry.getValue());
            items.add(item);
        }

        adminLiveUpdateService.publishBorrowRequested(
                borrowRequestService.requestBorrow(student.getStudentId(), staff.getStaffId(), items, dueDate)
        );
        studentSessionService.clearBorrowCart(session);
        redirectAttributes.addFlashAttribute("msg", "ÄÃ£ gá»­i yÃªu cáº§u mÆ°á»£n sÃ¡ch.");
        return "redirect:/borrows";
    }

    @PostMapping("/borrows/holds")
    public String placeHold(@RequestParam Integer bookId,
                            @RequestParam(required = false) String note,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        Student student = currentStudentService.resolveCurrentStudent(authentication)
                .orElseThrow(() -> new IllegalArgumentException("KhÃ´ng thá»ƒ xÃ¡c Ä‘á»‹nh sinh viÃªn hiá»‡n táº¡i."));
        bookHoldService.placeHold(student.getStudentId(), bookId, note);
        redirectAttributes.addFlashAttribute("msg", "ÄÃ£ táº¡o yÃªu cáº§u giá»¯ chá»—.");
        return "redirect:/borrows";
    }

    @PostMapping("/borrows/holds/{holdId}/cancel")
    public String cancelHold(@PathVariable Integer holdId,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        Student student = currentStudentService.resolveCurrentStudent(authentication)
                .orElseThrow(() -> new IllegalArgumentException("KhÃ´ng thá»ƒ xÃ¡c Ä‘á»‹nh sinh viÃªn hiá»‡n táº¡i."));
        bookHoldService.cancelHold(student.getStudentId(), holdId);
        redirectAttributes.addFlashAttribute("msg", "ÄÃ£ há»§y yÃªu cáº§u giá»¯ chá»—.");
        return "redirect:/borrows";
    }
}
