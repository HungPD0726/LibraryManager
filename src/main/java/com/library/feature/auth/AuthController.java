package com.library.feature.auth;

import com.library.domain.model.Staff;
import com.library.feature.staff.StaffService;
import com.library.feature.student.CurrentStudentService;
import com.library.feature.student.StudentMirrorService;
import com.library.shared.support.RoleSupport;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final StaffService staffService;
    private final StudentMirrorService studentMirrorService;
    private final CurrentStudentService currentStudentService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            @RequestParam(value = "reset", required = false) String reset,
                            @RequestParam(value = "expired", required = false) String expired,
                            Model model) {
        if (error != null) {
            model.addAttribute("error", "Tên đăng nhập, email hoặc mật khẩu chưa đúng.");
        }
        if (logout != null) {
            model.addAttribute("message", "Bạn đã đăng xuất thành công.");
        }
        if (reset != null) {
            model.addAttribute("message", "Mật khẩu đã được cập nhật. Vui lòng đăng nhập lại.");
        }
        if (expired != null) {
            model.addAttribute("error", "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
        }
        return "auth/login";
    }

    @GetMapping("/login/success")
    public String loginSuccess(Authentication authentication, RedirectAttributes redirectAttributes) {
        if (RoleSupport.isAdmin(authentication) || RoleSupport.isStaff(authentication)) {
            return "redirect:/admin/dashboard";
        }

        if (RoleSupport.isStudent(authentication)) {
            try {
                boolean resolved = currentStudentService.findCurrentStaff(authentication)
                        .map(studentMirrorService::ensureStudentMirror)
                        .isPresent();
                if (resolved) {
                    return "redirect:/home";
                }
                redirectAttributes.addFlashAttribute("error", "Không thể khởi tạo hồ sơ sinh viên cho tài khoản này.");
            } catch (RuntimeException ex) {
                redirectAttributes.addFlashAttribute("error", "Đăng nhập Google thành công nhưng không thể mở hồ sơ sinh viên.");
            }
            return "redirect:/login";
        }

        redirectAttributes.addFlashAttribute("error", "Tài khoản hiện tại không có quyền truy cập hệ thống.");
        return "redirect:/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new RegistrationForm());
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") RegistrationForm form,
                           BindingResult bindingResult,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "mismatch", "Xác nhận mật khẩu không khớp.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("form", form);
            return "auth/register";
        }

        Staff created = staffService.registerStudentAccount(
                form.getStaffName(),
                form.getUsername(),
                form.getEmail(),
                form.getPassword()
        );
        studentMirrorService.ensureStudentMirror(created);

        redirectAttributes.addFlashAttribute("message", "Đăng ký thành công. Bạn có thể đăng nhập ngay.");
        return "redirect:/login";
    }

    @GetMapping("/")
    public String root(Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }
        return "redirect:/login/success";
    }
}
