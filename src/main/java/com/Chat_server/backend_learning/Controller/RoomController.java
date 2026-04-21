package com.Chat_server.backend_learning.Controller;

import com.Chat_server.backend_learning.Entity.ChatMessage;
import com.Chat_server.backend_learning.Entity.Room;
import com.Chat_server.backend_learning.Entity.User;
import com.Chat_server.backend_learning.Repository.MessageRepository;
import com.Chat_server.backend_learning.Repository.RoomRepository;
import com.Chat_server.backend_learning.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin("*")
@RequiredArgsConstructor
public class RoomController {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    // Get username from JWT token — not from request param
    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @PostMapping("/create")
    public ResponseEntity<?> createRoom(@RequestBody Map<String, String> body) {
        String roomName = body.get("roomName");
        String username = getCurrentUsername(); // from JWT

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Room room = new Room();
        room.setRoomid(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        room.setName(roomName);
        room.getMembers().add(user);

        Room saved = roomRepository.save(room);
        return ResponseEntity.ok(Map.of(
                "roomId", saved.getRoomid(),
                "roomName", saved.getName()
        ));
    }



    @PostMapping("/join/{roomId}")
    public ResponseEntity<?> joinRoom(@PathVariable String roomId) {
        String username = getCurrentUsername(); // from JWT

        return roomRepository.findByRoomid(roomId).map(room -> {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (room.getMembers().contains(user)) {
                return ResponseEntity.ok(Map.of("message", "Already a member", "roomName", room.getName()));
            }

            room.getMembers().add(user);
            roomRepository.save(room);
            return ResponseEntity.ok(Map.of("message", "Joined successfully!", "roomName", room.getName()));
        }).orElse(ResponseEntity.badRequest().body(Map.of("error", "Room not found")));
    }

    @DeleteMapping("/leave/{roomId}")
    public ResponseEntity<?> leaveRoom(@PathVariable String roomId) {
        String username = getCurrentUsername();

        return roomRepository.findByRoomid(roomId).map(room -> {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            room.getMembers().remove(user);
            roomRepository.save(room);
            return ResponseEntity.ok(Map.of("message", "Left room successfully"));
        }).orElse(ResponseEntity.badRequest().body(Map.of("error", "Room not found")));
    }

    @GetMapping("/my-rooms")
    public ResponseEntity<?> getMyRooms() {
        String username = getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Map<String, String>> rooms = user.getRooms().stream()
                .map(r -> Map.of("roomId", r.getRoomid(), "roomName", r.getName()))
                .toList();

        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/history/{roomId}")
    public ResponseEntity<?> getChatHistory(@PathVariable String roomId) {
        String username = getCurrentUsername();

        Room room = roomRepository.findByRoomid(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        // Only members can view history
        boolean isMember = room.getMembers().stream()
                .anyMatch(u -> u.getUsername().equals(username));

        if (!isMember) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        // content is auto-decrypted by @Convert on ChatMessage.content
        List<Map<String, String>> messages = messageRepository
                .findByRoomRoomidOrderByTimestampAsc(roomId)
                .stream()
                .map(m -> Map.of(
                        "sender", m.getSender().getUsername(),
                        "content", m.getContent(), // already decrypted by converter
                        "timestamp", m.getTimestamp().toString()
                ))
                .toList();

        return ResponseEntity.ok(messages);
    }
}