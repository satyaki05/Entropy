package com.Chat_server.backend_learning.Repository;

import com.Chat_server.backend_learning.Entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByRoomRoomidOrderByTimestampAsc(String roomId);
}