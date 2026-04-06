package com.library.feature.chatbot;

import com.library.shared.support.RoleSupport;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<Map<String, String>> chat(@Valid @RequestBody ChatRequest request) {
        ChatbotService.ChatResult result = chatbotService.chat(
                request.messages().stream()
                        .map(message -> new ChatbotService.ChatMessage(message.role(), message.content()))
                        .toList()
        );
        return ResponseEntity.ok(Map.of("reply", result.reply(), "model", result.model()));
    }

    public record ChatRequest(
            @NotEmpty(message = "Danh sách tin nhắn không được để trống.")
            List<@NotNull(message = "Tin nhắn không hợp lệ.") @Valid ChatMessagePayload> messages
    ) {
    }

    public record ChatMessagePayload(
            @NotBlank(message = "Vai trò tin nhắn không được để trống.")
            @Pattern(regexp = "user|assistant", message = "Vai trò tin nhắn chỉ được là user hoặc assistant.")
            String role,
            @NotBlank(message = "Nội dung tin nhắn không được để trống.")
            @Size(max = ChatbotService.MAX_MESSAGE_LENGTH, message = "Nội dung tin nhắn không được vượt quá 2500 ký tự.")
            String content
    ) {
    }
}
