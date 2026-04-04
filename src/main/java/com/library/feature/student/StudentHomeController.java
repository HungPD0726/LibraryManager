package com.library.feature.student;

import com.library.domain.model.Student;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class StudentHomeController {

    private final CurrentStudentService currentStudentService;
    private final StudentHomeReadService studentHomeReadService;

    @GetMapping("/home")
    public String home(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) String search,
                       @RequestParam(required = false) String letter,
                       @RequestParam(required = false) Integer categoryId,
                       @RequestParam(required = false) Integer publisherId,
                       @RequestParam(required = false) String author,
                       Authentication authentication,
                       HttpSession session,
                       Model model) {
        Student student = currentStudentService.resolveCurrentStudent(authentication)
                .orElseThrow(() -> new IllegalArgumentException("Không thể xác định hồ sơ sinh viên hiện tại."));

        StudentHomePageView view = studentHomeReadService.buildPage(
                student, search, letter, categoryId, publisherId, author, page, session
        );

        model.addAttribute("books", view.books());
        model.addAttribute("categories", view.categories());
        model.addAttribute("publishers", view.publishers());
        model.addAttribute("currentPage", view.currentPage());
        model.addAttribute("totalPages", view.totalPages());
        model.addAttribute("search", view.search());
        model.addAttribute("letter", view.letter());
        model.addAttribute("categoryId", view.categoryId());
        model.addAttribute("publisherId", view.publisherId());
        model.addAttribute("author", view.author());
        model.addAttribute("summary", view.summary());
        model.addAttribute("studentDisplayName", view.studentDisplayName());
        return "student/home";
    }
}
