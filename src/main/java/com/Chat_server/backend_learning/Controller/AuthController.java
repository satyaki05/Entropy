package com.Chat_server.backend_learning.Controller;

import com.Chat_server.backend_learning.DTO.LoginReq;
import com.Chat_server.backend_learning.DTO.SignupReq;
import com.Chat_server.backend_learning.Entity.User;
import com.Chat_server.backend_learning.Repository.UserRepository;
import com.Chat_server.backend_learning.Util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupReq req) {
        System.out.println(">>> SIGNUP HIT SUCCESSFULLY!");
        System.out.println(">>> Username: " + req.getUsername());
        System.out.println(">>> Email: " + req.getEmail());

        try {
            if (userRepository.findByUsername(req.getUsername()).isPresent()) {
                System.out.println(">>> USERNAME ALREADY EXISTS");
                return ResponseEntity.badRequest().body("Username already exists!");
            }
            System.out.println(">>> SAVING USER...");
            User user = new User();
            user.setUsername(req.getUsername());
            user.setEmail(req.getEmail());
            user.setPassword(passwordEncoder.encode(req.getPassword()));
            userRepository.save(user);
            System.out.println(">>> USER SAVED SUCCESSFULLY");
            String token = jwtUtil.generateToken(user.getUsername());
            System.out.println(">>> TOKEN GENERATED");
            return ResponseEntity.ok(Map.of("token", token, "username", user.getUsername()));
        } catch (Exception e) {
            System.out.println(">>> ERROR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginReq req) {
        return userRepository.findByUsername(req.getUsername())
                .filter(u -> passwordEncoder.matches(req.getPassword(), u.getPassword()))
                .map(u -> {
                    String token = jwtUtil.generateToken(u.getUsername());
                    return ResponseEntity.ok(Map.of("token", token, "username", u.getUsername()));
                })
                .orElse(ResponseEntity.status(401).body(Map.of("error", "Invalid credentials")));
    }
}