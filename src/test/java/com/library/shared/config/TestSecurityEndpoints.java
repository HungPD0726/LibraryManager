package com.library.shared.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
class TestSecurityEndpoints {

    @GetMapping("/login")
    ResponseEntity<String> login() {
        return ResponseEntity.ok("login");
    }

    @GetMapping("/home")
    ResponseEntity<String> home() {
        return ResponseEntity.ok("home");
    }

    @GetMapping("/admin/orders")
    ResponseEntity<String> adminOrders() {
        return ResponseEntity.ok("orders");
    }

    @GetMapping("/api/pending-count")
    Map<String, Long> pendingCount() {
        return Map.of("pendingCount", 0L);
    }

    @GetMapping("/chatbot")
    ResponseEntity<String> chatbotPage() {
        return ResponseEntity.ok("chatbot-page");
    }

    @PostMapping("/chatbot")
    Map<String, String> chatbot(@RequestBody(required = false) Map<String, Object> body) {
        return Map.of("reply", "ok");
    }
}
