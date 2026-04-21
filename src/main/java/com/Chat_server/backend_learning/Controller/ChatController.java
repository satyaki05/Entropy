package com.Chat_server.backend_learning.Controller;

import com.Chat_server.backend_learning.Entity.ChatMessage;
import com.Chat_server.backend_learning.Entity.Room;
import com.Chat_server.backend_learning.Entity.User;
import com.Chat_server.backend_learning.Repository.MessageRepository;
import com.Chat_server.backend_learning.Repository.RoomRepository;
import com.Chat_server.backend_learning.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageRepository messageRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    // DTO for incoming WebSocket message payload
    public static class IncomingMessage {
        public String content;
        public String senderUsername; // frontend sends username as string
        public ChatMessage.MessageType type;
    }

    @MessageMapping("/chat.sendMessage/{roomId}")
    public void sendMessage(@DestinationVariable String roomId,
                            @Payload IncomingMessage incoming) {

        Room room = roomRepository.findByRoomid(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found: " + roomId));

        User sender = userRepository.findByUsername(incoming.senderUsername)
                .orElseThrow(() -> new RuntimeException("User not found: " + incoming.senderUsername));

        ChatMessage message = new ChatMessage();
        message.setContent(incoming.content);
        message.setSender(sender);
        message.setRoom(room);
        message.setType(ChatMessage.MessageType.CHAT);
        message.setTimestamp(LocalDateTime.now());

        // @Convert on content field auto-encrypts before DB save
        messageRepository.save(message);

        // Broadcast to room — send only safe fields to frontend
        messagingTemplate.convertAndSend("/topic/room/" + roomId,
                new OutgoingMessage(sender.getUsername(), incoming.content, "CHAT", message.getTimestamp().toString()));
    }

    @MessageMapping("/chat.join/{roomId}")
    public void joinRoom(@DestinationVariable String roomId,
                         @Payload IncomingMessage incoming) {
        messagingTemplate.convertAndSend("/topic/room/" + roomId,
                new OutgoingMessage(incoming.senderUsername, null, "JOIN", LocalDateTime.now().toString()));
    }

    @MessageMapping("/chat.leave/{roomId}")
    public void leaveRoom(@DestinationVariable String roomId,
                          @Payload IncomingMessage incoming) {
        messagingTemplate.convertAndSend("/topic/room/" + roomId,
                new OutgoingMessage(incoming.senderUsername, null, "LEAVE", LocalDateTime.now().toString()));
    }

    // Clean outgoing DTO — never expose full entity to frontend
    public record OutgoingMessage(String sender, String content, String type, String timestamp) {}
}