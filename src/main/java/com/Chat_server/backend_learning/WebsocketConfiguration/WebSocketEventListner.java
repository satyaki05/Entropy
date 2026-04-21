package com.Chat_server.backend_learning.WebsocketConfiguration;

import com.Chat_server.backend_learning.Controller.ChatController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListner {

    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) accessor.getSessionAttributes().get("username");
        String roomId = (String) accessor.getSessionAttributes().get("roomId");

        if (username != null && roomId != null) {
            log.info("User disconnected: {} from room: {}", username, roomId);
            messagingTemplate.convertAndSend("/topic/room/" + roomId,
                    new ChatController.OutgoingMessage(username, null, "LEAVE",
                            java.time.LocalDateTime.now().toString()));
        }
    }
}