package com.library.feature.auth;

import com.library.feature.auth.PasswordRecoveryService;
import com.library.feature.auth.ForgotPasswordForm;
import com.library.feature.auth.ResetPasswordForm;
import com.library.feature.auth.VerifyOtpForm;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class PasswordRecoveryController {

    private final PasswordRecoveryService passwordRecoveryService;

    @GetMapping("/forgot-password")
    public String forgotPasswordPage(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new ForgotPasswordForm());
        }
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@Valid @ModelAttribute("form") ForgotPasswordForm form,
                                 BindingResult bindingResult,
                                 HttpSession session,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "auth/forgot-password";
        }

        try {
            passwordRecoveryService.startReset(form.getIdentity(), session);
            redirectAttributes.addFlashAttribute("message", "OTP Ã„â€˜ÃƒÂ£ Ã„â€˜Ã†Â°Ã¡Â»Â£c gÃ¡Â»Â­i tÃ¡Â»â€ºi email Ã„â€˜Ã„Æ’ng kÃƒÂ½.");
            return "redirect:/verify-otp";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            return "auth/forgot-password";
        }
    }

    @GetMapping("/verify-otp")
    public String verifyOtpPage(HttpSession session, Model model) {
        if (!passwordRecoveryService.hasPendingReset(session)) {
            return "redirect:/forgot-password";
        }
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new VerifyOtpForm());
        }
        model.addAttribute("pendingUsername", passwordRecoveryService.getPendingUsername(session));
        return "auth/verify-otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@Valid @ModelAttribute("form") VerifyOtpForm form,
                            BindingResult bindingResult,
                            HttpSession session,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        if (!passwordRecoveryService.hasPendingReset(session)) {
            return "redirect:/forgot-password";
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("pendingUsername", passwordRecoveryService.getPendingUsername(session));
            return "auth/verify-otp";
        }

        try {
            passwordRecoveryService.verifyOtp(form.getOtp(), session);
            redirectAttributes.addFlashAttribute("message", "XÃƒÂ¡c minh OTP thÃƒÂ nh cÃƒÂ´ng. Vui lÃƒÂ²ng nhÃ¡ÂºÂ­p mÃ¡ÂºÂ­t khÃ¡ÂºÂ©u mÃ¡Â»â€ºi.");
            return "redirect:/reset-password";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("pendingUsername", passwordRecoveryService.getPendingUsername(session));
            return "auth/verify-otp";
        }
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(HttpSession session, Model model) {
        if (!passwordRecoveryService.isVerified(session)) {
            return "redirect:/forgot-password";
        }
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new ResetPasswordForm());
        }
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@Valid @ModelAttribute("form") ResetPasswordForm form,
                                BindingResult bindingResult,
                                HttpSession session,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (!passwordRecoveryService.isVerified(session)) {
            return "redirect:/forgot-password";
        }
        if (bindingResult.hasErrors()) {
            return "auth/reset-password";
        }

        try {
            passwordRecoveryService.resetPassword(form.getPassword(), form.getConfirmPassword(), session);
            redirectAttributes.addFlashAttribute("message", "MÃ¡ÂºÂ­t khÃ¡ÂºÂ©u Ã„â€˜ÃƒÂ£ Ã„â€˜Ã†Â°Ã¡Â»Â£c cÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t.");
            return "redirect:/login?reset=true";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            return "auth/reset-password";
        }
    }
}
