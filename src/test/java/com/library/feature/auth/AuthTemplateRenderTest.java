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
                .contains("data-toggle-password=\"#loginPassword\"")
                .doesNotContain("/libraryManager/js/app.js");
    }
}
