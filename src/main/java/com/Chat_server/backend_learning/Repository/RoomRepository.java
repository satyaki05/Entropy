package com.Chat_server.backend_learning.Repository;

import com.Chat_server.backend_learning.Entity.Room;
import com.Chat_server.backend_learning.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByRoomid(String roomid);
}
