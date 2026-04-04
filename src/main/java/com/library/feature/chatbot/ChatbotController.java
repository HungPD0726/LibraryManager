package com.library.feature.chatbot;

import com.library.shared.support.RoleSupport;
import com.library.feature.chatbot.ChatbotService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @GetMapping("/chatbot")
    public String page(Authentication authentication, HttpServletRequest request, Model model) {
        model.addAttribute("chatbotConfigured", chatbotService.isConfigured());
        model.addAttribute("chatbotModel", chatbotService.getModel());
        model.addAttribute("viewerName", authentication != null ? authentication.getName() : "user");
        model.addAttribute("requestPath", request.getRequestURI());
        model.addAttribute("currentUsername", authentication != null ? authentication.getName() : null);
        model.addAttribute("isAdmin", authentication != null && RoleSupport.isAdmin(authentication));
        model.addAttribute("isStaff", authentication != null && RoleSupport.isStaff(authentication));
        model.addAttribute("isStudent", authentication != null && RoleSupport.isStudent(authentication));
        return "student/chatbot";
    }

    @PostMapping(value = "/chatbot", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> chat(@RequestBody(required = false) ChatRequest request) {
        try {
            List<ChatMessagePayload> messages = request == null || request.messages() == null
                    ? List.of()
                    : request.messages();
            ChatbotService.ChatResult result = chatbotService.chat(
                    messages.stream()
                            .map(message -> new ChatbotService.ChatMessage(message.role(), message.content()))
                            .toList()
            );
            return ResponseEntity.ok(Map.of("reply", result.reply(), "model", result.model()));
        } catch (ChatbotService.ChatbotException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        }
    }

    public record ChatRequest(List<ChatMessagePayload> messages) {
    }

    public record ChatMessagePayload(String role, String content) {
    }
}
