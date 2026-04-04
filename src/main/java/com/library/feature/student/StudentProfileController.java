package com.library.feature.student;

import com.library.domain.model.Staff;
import com.library.domain.model.Student;
import com.library.feature.staff.StaffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class StudentProfileController {

    private final CurrentStudentService currentStudentService;
    private final StudentProfileService studentProfileService;
    private final StaffService staffService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public String profile(Authentication authentication, Model model) {
        Student student = currentStudentService.resolveCurrentStudent(authentication)
                .orElseThrow(() -> new IllegalArgumentException("Không thể xác định sinh viên hiện tại."));

        if (!model.containsAttribute("profileForm")) {
            ProfileForm form = new ProfileForm();
            form.setStudentName(student.getStudentName());
            form.setEmail(student.getEmail());
            form.setPhone(student.getPhone());
            model.addAttribute("profileForm", form);
        }
        if (!model.containsAttribute("passwordForm")) {
            model.addAttribute("passwordForm", new ChangePasswordForm());
        }
        model.addAttribute("studentProfile", student);
        return "student/profile";
    }

    @PostMapping
    public String updateProfile(@Valid @ModelAttribute("profileForm") ProfileForm profileForm,
                                BindingResult bindingResult,
                                Authentication authentication,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        Staff staff = currentStudentService.findCurrentStaff(authentication)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản đăng nhập."));
        Student student = currentStudentService.resolveCurrentStudent(authentication)
                .orElseThrow(() -> new IllegalArgumentException("Không thể xác định sinh viên hiện tại."));

        if (bindingResult.hasErrors()) {
            model.addAttribute("studentProfile", student);
            model.addAttribute("passwordForm", new ChangePasswordForm());
            return "student/profile";
        }

        studentProfileService.updateProfile(staff, profileForm);
        redirectAttributes.addFlashAttribute("msg", "Cập nhật hồ sơ thành công.");
        return "redirect:/profile";
    }

    @PostMapping("/change-password")
    public String changePassword(@Valid @ModelAttribute("passwordForm") ChangePasswordForm passwordForm,
                                 BindingResult bindingResult,
                                 Authentication authentication,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        Staff staff = currentStudentService.findCurrentStaff(authentication)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản đăng nhập."));
        Student student = currentStudentService.resolveCurrentStudent(authentication)
                .orElseThrow(() -> new IllegalArgumentException("Không thể xác định sinh viên hiện tại."));

        if (!passwordEncoder.matches(passwordForm.getCurrentPassword(), staff.getPassword())) {
            bindingResult.rejectValue("currentPassword", "invalid", "Mật khẩu hiện tại chưa đúng.");
        }
        if (!passwordForm.getNewPassword().equals(passwordForm.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "mismatch", "Xác nhận mật khẩu không khớp.");
        }

        if (bindingResult.hasErrors()) {
            ProfileForm profileForm = new ProfileForm();
            profileForm.setStudentName(student.getStudentName());
            profileForm.setEmail(student.getEmail());
            profileForm.setPhone(student.getPhone());
            model.addAttribute("profileForm", profileForm);
            model.addAttribute("studentProfile", student);
            return "student/profile";
        }

        staffService.updatePassword(staff, passwordForm.getNewPassword());
        redirectAttributes.addFlashAttribute("msg", "Đổi mật khẩu thành công.");
        return "redirect:/profile";
    }
}
