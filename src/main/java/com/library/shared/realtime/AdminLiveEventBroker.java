package com.library.shared.realtime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class AdminLiveEventBroker {

    private final ObjectMapper objectMapper;
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    public void register(WebSocketSession session) {
        if (session != null && session.isOpen()) {
            sessions.add(session);
        }
    }

    public void unregister(WebSocketSession session) {
        if (session != null) {
            sessions.remove(session);
        }
    }

    public void broadcast(AdminLiveEvent event) {
        if (event == null || sessions.isEmpty()) {
            return;
        }

        TextMessage message = new TextMessage(toJson(event));
        for (WebSocketSession session : sessions) {
            if (!session.isOpen()) {
                sessions.remove(session);
                continue;
            }
            try {
                synchronized (session) {
                    session.sendMessage(message);
                }
            } catch (IOException ex) {
                sessions.remove(session);
                try {
                    session.close();
                } catch (IOException ignored) {
                    // Best-effort cleanup for dead websocket sessions.
                }
            }
        }
    }

    private String toJson(AdminLiveEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Cannot serialize admin live event.", ex);
        }
    }
}
