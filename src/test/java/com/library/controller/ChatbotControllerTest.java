package com.library.controller;

import com.library.config.CommonModelAdvice;
import com.library.config.SecurityConfig;
import com.library.repository.StaffRepository;
import com.library.service.ChatbotService;
import com.library.service.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = ChatbotController.class,
        properties = "spring.main.allow-bean-definition-overriding=true",
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = CommonModelAdvice.class))
@Import({SecurityConfig.class, ChatbotControllerTest.TestBeans.class})
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
        chatbotService.configure(true, "llama-3.1-8b-instant");

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
        org.assertj.core.api.Assertions.assertThat(messages)
                .hasSize(1)
                .first()
                .satisfies(message -> {
                    org.assertj.core.api.Assertions.assertThat(message.role()).isEqualTo("user");
                    org.assertj.core.api.Assertions.assertThat(message.content()).isEqualTo("Tôi muốn mượn sách");
                });
    }

    @Test
    void postChat_shouldReturnBadRequestWhenServiceFails() throws Exception {
        chatbotService.willThrow(new ChatbotService.ChatbotException("Chatbot chưa được cấu hình GROQ_API_KEY."));

        mockMvc.perform(post("/chatbot")
                        .with(SecurityMockMvcRequestPostProcessors.user("student01").roles("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"messages":[{"role":"user","content":"Xin chào"}]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Chatbot chưa được cấu hình GROQ_API_KEY."));
    }

    @TestConfiguration
    static class TestBeans {

        @Bean
        CustomUserDetailsService customUserDetailsService() {
            StaffRepository repository = Mockito.mock(StaffRepository.class);
            return new CustomUserDetailsService(repository);
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
    }
}
