package com.library.feature.auth;

import com.library.support.ThymeleafRenderSupport;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AuthTemplateRenderTest {

    @Test
    void login_shouldRenderAuthAssetsAndFlashPayload() {
        Map<String, Object> model = new HashMap<>();
        model.put("error", "Invalid login");

        String html = ThymeleafRenderSupport.render(
                "auth/login",
                "/login",
                model,
                null
        );

        assertThat(html)
                .contains("/libraryManager/css/style.css")
                .contains("/libraryManager/css/parts/auth.css")
                .contains("/libraryManager/js/auth.js")
                .contains("data-flash-error=\"Invalid login\"")
                .contains("auth-showcase-stats")
                .contains("data-toggle-password=\"#loginPassword\"")
                .contains("/libraryManager/oauth2/authorization/google")
                .contains("Cổng thông tin Thư viện")
                .contains("trợ lý thư viện")
                .contains("Theo dõi mượn trả, đơn mua và trợ lý thư viện")
                .doesNotContain("Chatbot")
                .doesNotContain("Assistant")
                .doesNotContain("Library AI")
                .doesNotContain("Student Portal")
                .doesNotContain("Admin Console")
                .doesNotContain("Ãƒ")
                .doesNotContain("Ã‚")
                .doesNotContain("/libraryManager/js/app.js");
    }
}
