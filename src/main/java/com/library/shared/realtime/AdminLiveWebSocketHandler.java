package com.library.shared.realtime;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class AdminLiveWebSocketHandler extends TextWebSocketHandler {

    private final AdminLiveEventBroker adminLiveEventBroker;

    public AdminLiveWebSocketHandler(AdminLiveEventBroker adminLiveEventBroker) {
        this.adminLiveEventBroker = adminLiveEventBroker;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        if (!hasAdminAccess(session)) {
            session.close(new CloseStatus(HttpStatus.FORBIDDEN.value(), "Forbidden"));
            return;
        }
        adminLiveEventBroker.register(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        adminLiveEventBroker.unregister(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        adminLiveEventBroker.unregister(session);
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    private boolean hasAdminAccess(WebSocketSession session) {
        if (!(session.getPrincipal() instanceof Authentication authentication)) {
            return false;
        }

        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String value = authority.getAuthority();
            if ("ROLE_ADMIN".equals(value) || "ROLE_STAFF".equals(value) || "ROLE_LIBRARIAN".equals(value)) {
                return true;
            }
        }
        return false;
    }
}
