package com.library.feature.student;

import com.library.domain.model.Student;
import com.library.feature.student.StudentService;
import com.library.feature.student.StudentForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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

@Controller
@RequestMapping("/admin/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) String search,
                       Model model) {
        Page<Student> studentPage = (search != null && !search.isBlank())
                ? studentService.search(search, page, 10)
                : studentService.findAll(page, 10);

        model.addAttribute("students", studentPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", studentPage.getTotalPages());
        model.addAttribute("search", search);
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new StudentForm());
        }
        return "admin/student/list";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("form") StudentForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("students", studentService.findAll(0, 10).getContent());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", studentService.findAll(0, 10).getTotalPages());
            return "admin/student/list";
        }

        Student student = new Student();
        student.setStudentName(form.getStudentName().trim());
        student.setEmail(blankToNull(form.getEmail()));
        student.setPhone(blankToNull(form.getPhone()));
        studentService.save(student);
        redirectAttributes.addFlashAttribute("msg", "Thêm sinh viên thành công.");
        return "redirect:/admin/students";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Integer id,
                       @Valid @ModelAttribute("form") StudentForm form,
                       BindingResult bindingResult,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("students", studentService.findAll(0, 10).getContent());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", studentService.findAll(0, 10).getTotalPages());
            return "admin/student/list";
        }

        Student student = studentService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sinh viên cần cập nhật."));
        student.setStudentName(form.getStudentName().trim());
        student.setEmail(blankToNull(form.getEmail()));
        student.setPhone(blankToNull(form.getPhone()));
        studentService.save(student);
        redirectAttributes.addFlashAttribute("msg", "Cập nhật sinh viên thành công.");
        return "redirect:/admin/students";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        studentService.deleteById(id);
        redirectAttributes.addFlashAttribute("msg", "Xóa sinh viên thành công.");
        return "redirect:/admin/students";
    }

    private String blankToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
