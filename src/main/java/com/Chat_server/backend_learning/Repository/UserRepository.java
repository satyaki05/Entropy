package com.Chat_server.backend_learning.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.Chat_server.backend_learning.Entity.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);
}