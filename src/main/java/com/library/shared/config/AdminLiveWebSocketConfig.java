package com.library.shared.config;

import com.library.shared.realtime.AdminLiveWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class AdminLiveWebSocketConfig implements WebSocketConfigurer {

    private final AdminLiveWebSocketHandler adminLiveWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(adminLiveWebSocketHandler, "/ws/admin/live")
                .setAllowedOriginPatterns("*");
    }
}
