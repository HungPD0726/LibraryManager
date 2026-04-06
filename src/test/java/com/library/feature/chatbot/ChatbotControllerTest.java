package com.library.feature.chatbot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.domain.model.Staff;
import com.library.domain.model.Student;
import com.library.domain.repository.RoleRepository;
import com.library.domain.repository.StaffRepository;
import com.library.feature.auth.CustomOAuth2UserService;
import com.library.feature.auth.CustomUserDetailsService;
import com.library.feature.student.StudentMirrorService;
import com.library.shared.config.ApiExceptionHandler;
import com.library.shared.config.CommonModelAdvice;
import com.library.shared.config.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = ChatbotController.class,
        properties = "spring.main.allow-bean-definition-overriding=true",
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = CommonModelAdvice.class))
@Import({SecurityConfig.class, ApiExceptionHandler.class, ChatbotControllerTest.TestBeans.class})
class ChatbotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestBeans.FakeChatbotService chatbotService;

    @BeforeEach
    void setUp() {
        chatbotService.configure(true, "llama-3.1-8b-instant");
        chatbotService.willReturn(new ChatbotService.ChatResult("OK", "llama-3.1-8b-instant"));
    }

    @Test
    void page_shouldExposeChatbotConfiguration() throws Exception {
        mockMvc.perform(get("/chatbot")
                        .with(SecurityMockMvcRequestPostProcessors.user("student01").roles("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(view().name("student/chatbot"))
                .andExpect(model().attribute("chatbotConfigured", true))
                .andExpect(model().attribute("chatbotModel", "llama-3.1-8b-instant"))
                .andExpect(model().attribute("viewerName", "student01"));
    }

    @Test
    void postChat_shouldReturnReplyPayload() throws Exception {
        chatbotService.willReturn(new ChatbotService.ChatResult(
                "Bạn có thể mở mục Mượn sách để bắt đầu.",
                "llama-3.1-8b-instant"
        ));

        mockMvc.perform(post("/chatbot")
                        .with(SecurityMockMvcRequestPostProcessors.user("student01").roles("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"messages":[{"role":"user","content":"Tôi muốn mượn sách"}]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply").value("Bạn có thể mở mục Mượn sách để bắt đầu."))
                .andExpect(jsonPath("$.model").value("llama-3.1-8b-instant"));

        List<ChatbotService.ChatMessage> messages = chatbotService.getLastMessages();
        assertThat(messages)
                .hasSize(1)
                .first()
                .satisfies(message -> {
                    assertThat(message.role()).isEqualTo("user");
                    assertThat(message.content()).isEqualTo("Tôi muốn mượn sách");
                });
    }

    @Test
    void postChat_shouldReturnServiceUnavailableWhenChatbotIsNotConfigured() throws Exception {
        chatbotService.willThrow(ChatbotService.ChatbotException.serviceUnavailable(
                "Chatbot chưa được cấu hình GROQ_API_KEY."
        ));

        mockMvc.perform(post("/chatbot")
                        .with(SecurityMockMvcRequestPostProcessors.user("student01").roles("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"messages":[{"role":"user","content":"Xin chào"}]}
                                """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("Chatbot chưa được cấu hình GROQ_API_KEY."))
                .andExpect(jsonPath("$.code").value("CHATBOT_NOT_CONFIGURED"))
                .andExpect(jsonPath("$.path").value("/chatbot"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void postChat_shouldReturnBadRequestWhenBodyIsEmpty() throws Exception {
        mockMvc.perform(post("/chatbot")
                        .with(SecurityMockMvcRequestPostProcessors.user("student01").roles("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Nội dung JSON không hợp lệ."))
                .andExpect(jsonPath("$.code").value("INVALID_JSON"))
                .andExpect(jsonPath("$.path").value("/chatbot"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void postChat_shouldReturnBadRequestWhenJsonIsMalformed() throws Exception {
        mockMvc.perform(post("/chatbot")
                        .with(SecurityMockMvcRequestPostProcessors.user("student01").roles("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Nội dung JSON không hợp lệ."))
                .andExpect(jsonPath("$.code").value("INVALID_JSON"));
    }

    @Test
    void postChat_shouldReturnBadRequestWhenMessagesAreEmpty() throws Exception {
        mockMvc.perform(post("/chatbot")
                        .with(SecurityMockMvcRequestPostProcessors.user("student01").roles("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"messages":[]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Danh sách tin nhắn không được để trống."))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void postChat_shouldReturnBadRequestWhenRoleIsInvalid() throws Exception {
        mockMvc.perform(post("/chatbot")
                        .with(SecurityMockMvcRequestPostProcessors.user("student01").roles("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"messages":[{"role":"system","content":"Xin chào"}]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Vai trò tin nhắn chỉ được là user hoặc assistant."))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void postChat_shouldReturnBadRequestWhenContentIsBlank() throws Exception {
        mockMvc.perform(post("/chatbot")
                        .with(SecurityMockMvcRequestPostProcessors.user("student01").roles("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"messages":[{"role":"user","content":"   "}]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Nội dung tin nhắn không được để trống."))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void postChat_shouldReturnBadGatewayWhenGroqFails() throws Exception {
        chatbotService.willThrow(ChatbotService.ChatbotException.badGateway(
                "Groq trả về lỗi 429: rate limit."
        ));

        mockMvc.perform(post("/chatbot")
                        .with(SecurityMockMvcRequestPostProcessors.user("student01").roles("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"messages":[{"role":"user","content":"Xin chào"}]}
                                """))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error").value("Groq trả về lỗi 429: rate limit."))
                .andExpect(jsonPath("$.code").value("GROQ_UPSTREAM_ERROR"))
                .andExpect(jsonPath("$.path").value("/chatbot"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @TestConfiguration
    static class TestBeans {

        @Bean
        CustomUserDetailsService customUserDetailsService() {
            StaffRepository repository = Mockito.mock(StaffRepository.class);
            return new CustomUserDetailsService(repository);
        }

        @Bean
        CustomOAuth2UserService customOAuth2UserService() {
            return new CustomOAuth2UserService(
                    Mockito.mock(StaffRepository.class),
                    Mockito.mock(RoleRepository.class),
                    new NoOpStudentMirrorService()
            );
        }

        @Bean
        FakeChatbotService chatbotService() {
            return new FakeChatbotService();
        }

        static class FakeChatbotService extends ChatbotService {

            private boolean configured = true;
            private String model = "llama-3.1-8b-instant";
            private ChatResult nextResult = new ChatResult("OK", "llama-3.1-8b-instant");
            private RuntimeException nextException;
            private List<ChatMessage> lastMessages = List.of();

            FakeChatbotService() {
                super(new ObjectMapper(), 1000, 1000, 700, 0.4d);
            }

            @Override
            public boolean isConfigured() {
                return configured;
            }

            @Override
            public String getModel() {
                return model;
            }

            @Override
            public ChatResult chat(List<ChatMessage> messages) {
                lastMessages = List.copyOf(messages);
                if (nextException != null) {
                    throw nextException;
                }
                return nextResult;
            }

            void configure(boolean configured, String model) {
                this.configured = configured;
                this.model = model;
            }

            void willReturn(ChatResult result) {
                this.nextException = null;
                this.nextResult = result;
            }

            void willThrow(RuntimeException exception) {
                this.nextException = exception;
            }

            List<ChatMessage> getLastMessages() {
                return lastMessages;
            }
        }

        static class NoOpStudentMirrorService extends StudentMirrorService {

            NoOpStudentMirrorService() {
                super(null, null);
            }

            @Override
            public Student ensureStudentMirror(Staff staff) {
                return new Student();
            }
        }
    }
}
