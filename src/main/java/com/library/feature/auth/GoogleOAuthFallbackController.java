package com.library.feature.auth;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@ConditionalOnMissingBean(ClientRegistrationRepository.class)
public class GoogleOAuthFallbackController {

    @GetMapping("/oauth2/authorization/google")
    public String redirectWhenGoogleOauthUnavailable(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "Dang nhap Google chua duoc cau hinh tren may nay.");
        return "redirect:/login";
    }
}
