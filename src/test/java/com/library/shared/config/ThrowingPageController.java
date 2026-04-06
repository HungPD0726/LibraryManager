package com.library.shared.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ThrowingPageController {

    @GetMapping("/test/error")
    String error() {
        throw new IllegalArgumentException("Trang mẫu bị lỗi.");
    }
}
