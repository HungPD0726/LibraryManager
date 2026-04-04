package com.library.feature.chatbot;

import com.library.shared.config.CommonModelAdvice;
import com.library.shared.config.SecurityConfig;
import com.library.domain.repository.StaffRepository;
import com.library.feature.chatbot.ChatbotService;
import com.library.feature.auth.CustomUserDetailsService;
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
                "BÃ¡ÂºÂ¡n cÃƒÂ³ thÃ¡Â»Æ’ mÃ¡Â»Å¸ mÃ¡Â»Â¥c MÃ†Â°Ã¡Â»Â£n sÃƒÂ¡ch Ã„â€˜Ã¡Â»Æ’ bÃ¡ÂºÂ¯t Ã„â€˜Ã¡ÂºÂ§u.",
                "llama-3.1-8b-instant"
        ));

        mockMvc.perform(post("/chatbot")
                        .with(SecurityMockMvcRequestPostProcessors.user("student01").roles("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"messages":[{"role":"user","content":"TÃƒÂ´i muÃ¡Â»â€˜n mÃ†Â°Ã¡Â»Â£n sÃƒÂ¡ch"}]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply").value("BÃ¡ÂºÂ¡n cÃƒÂ³ thÃ¡Â»Æ’ mÃ¡Â»Å¸ mÃ¡Â»Â¥c MÃ†Â°Ã¡Â»Â£n sÃƒÂ¡ch Ã„â€˜Ã¡Â»Æ’ bÃ¡ÂºÂ¯t Ã„â€˜Ã¡ÂºÂ§u."))
                .andExpect(jsonPath("$.model").value("llama-3.1-8b-instant"));

        List<ChatbotService.ChatMessage> messages = chatbotService.getLastMessages();
        org.assertj.core.api.Assertions.assertThat(messages)
                .hasSize(1)
                .first()
                .satisfies(message -> {
                    org.assertj.core.api.Assertions.assertThat(message.role()).isEqualTo("user");
                    org.assertj.core.api.Assertions.assertThat(message.content()).isEqualTo("TÃƒÂ´i muÃ¡Â»â€˜n mÃ†Â°Ã¡Â»Â£n sÃƒÂ¡ch");
                });
    }

    @Test
    void postChat_shouldReturnBadRequestWhenServiceFails() throws Exception {
        chatbotService.willThrow(new ChatbotService.ChatbotException("Chatbot chÃ†Â°a Ã„â€˜Ã†Â°Ã¡Â»Â£c cÃ¡ÂºÂ¥u hÃƒÂ¬nh GROQ_API_KEY."));

        mockMvc.perform(post("/chatbot")
                        .with(SecurityMockMvcRequestPostProcessors.user("student01").roles("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"messages":[{"role":"user","content":"Xin chÃƒÂ o"}]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Chatbot chÃ†Â°a Ã„â€˜Ã†Â°Ã¡Â»Â£c cÃ¡ÂºÂ¥u hÃƒÂ¬nh GROQ_API_KEY."));
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
