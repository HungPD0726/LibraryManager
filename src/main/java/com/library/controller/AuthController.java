package com.library.controller;

import com.library.entity.Staff;
import com.library.service.StaffService;
import com.library.service.StudentContextService;
import com.library.support.RoleSupport;
import com.library.web.form.RegistrationForm;
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
    private final StudentContextService studentContextService;

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
    public String loginSuccess(Authentication authentication) {
        if (RoleSupport.isAdmin(authentication) || RoleSupport.isStaff(authentication)) {
            return "redirect:/admin/dashboard";
        }
        if (RoleSupport.isStudent(authentication)) {
            return "redirect:/home";
        }
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
        studentContextService.ensureStudentMirror(created);

        redirectAttributes.addFlashAttribute("message", "Đăng ký thành công. Bạn có thể đăng nhập ngay.");
        return "redirect:/login";
    }

    @GetMapping("/")
    public String root(Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }
        return loginSuccess(authentication);
    }
}
