package com.library.feature.order;

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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class StudentBuyController {

    private final StudentBuyReadService studentBuyReadService;
    private final OrderCheckoutService orderCheckoutService;
    private final CurrentStudentService currentStudentService;
    private final StudentSessionService studentSessionService;
    private final AdminLiveUpdateService adminLiveUpdateService;

    @GetMapping("/buy")
    public String buyCenter(Authentication authentication, HttpSession session, Model model) {
        Student student = currentStudentService.resolveCurrentStudent(authentication)
                .orElseThrow(() -> new IllegalArgumentException("KhÃ´ng thá»ƒ xÃ¡c Ä‘á»‹nh sinh viÃªn hiá»‡n táº¡i."));

        StudentBuyPageView view = studentBuyReadService.buildPage(
                student.getStudentId(),
                studentSessionService.waitlist(session)
        );
        model.addAttribute("bookPrices", view.bookPrices());
        model.addAttribute("waitlistItems", view.waitlistItems());
        model.addAttribute("orderHistory", view.orderHistory());
        return "student/buy";
    }

    @PostMapping("/buy/waitlist/add")
    public String addToWaitlist(@RequestParam Integer bookId,
                                @RequestParam(defaultValue = "1") Integer quantity,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Map<Integer, Integer> waitlist = studentSessionService.waitlist(session);
        waitlist.put(bookId, waitlist.getOrDefault(bookId, 0) + Math.max(1, quantity));
        redirectAttributes.addFlashAttribute("msg", "ÄÃ£ thÃªm sÃ¡ch vÃ o danh sÃ¡ch chá» mua.");
        return "redirect:/buy";
    }

    @PostMapping("/buy/waitlist/update/{bookId}")
    public String updateWaitlist(@PathVariable Integer bookId,
                                 @RequestParam Integer quantity,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Sá»‘ lÆ°á»£ng pháº£i lá»›n hÆ¡n 0.");
        }
        studentSessionService.waitlist(session).put(bookId, quantity);
        redirectAttributes.addFlashAttribute("msg", "ÄÃ£ cáº­p nháº­t sá»‘ lÆ°á»£ng mua.");
        return "redirect:/buy";
    }

    @PostMapping("/buy/waitlist/remove/{bookId}")
    public String removeWaitlist(@PathVariable Integer bookId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        studentSessionService.waitlist(session).remove(bookId);
        redirectAttributes.addFlashAttribute("msg", "ÄÃ£ xÃ³a sÃ¡ch khá»i danh sÃ¡ch chá» mua.");
        return "redirect:/buy";
    }

    @PostMapping("/buy/checkout")
    public String checkout(@RequestParam("selectedBookIds") List<Integer> selectedBookIds,
                           Authentication authentication,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        if (selectedBookIds == null || selectedBookIds.isEmpty()) {
            throw new IllegalArgumentException("Vui lÃ²ng chá»n Ã­t nháº¥t má»™t sÃ¡ch Ä‘á»ƒ Ä‘áº·t.");
        }

        Student student = currentStudentService.resolveCurrentStudent(authentication)
                .orElseThrow(() -> new IllegalArgumentException("KhÃ´ng thá»ƒ xÃ¡c Ä‘á»‹nh sinh viÃªn hiá»‡n táº¡i."));
        Integer staffId = currentStudentService.findCurrentStaff(authentication)
                .map(staff -> staff.getStaffId())
                .orElseThrow(() -> new IllegalArgumentException("KhÃ´ng thá»ƒ xÃ¡c Ä‘á»‹nh tÃ i khoáº£n Ä‘Äƒng nháº­p."));

        Map<Integer, Integer> currentWaitlist = studentSessionService.waitlist(session);
        Map<Integer, Integer> orderItems = new LinkedHashMap<>();
        for (Integer bookId : selectedBookIds) {
            Integer quantity = currentWaitlist.get(bookId);
            if (quantity != null && quantity > 0) {
                orderItems.put(bookId, quantity);
            }
        }
        if (orderItems.isEmpty()) {
            throw new IllegalArgumentException("KhÃ´ng cÃ³ sÃ¡ch há»£p lá»‡ Ä‘á»ƒ Ä‘áº·t.");
        }

        adminLiveUpdateService.publishOrderCreated(
                orderCheckoutService.createStudentOrder(student.getStudentId(), staffId, orderItems)
        );
        orderItems.keySet().forEach(currentWaitlist::remove);
        redirectAttributes.addFlashAttribute("msg", "ÄÃ£ táº¡o Ä‘Æ¡n mua sÃ¡ch thÃ nh cÃ´ng.");
        return "redirect:/buy";
    }
}
