package com.jobportal.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jobportal.backend.dto.AuthResponse;
import com.jobportal.backend.dto.LoginRequest;
import com.jobportal.backend.dto.RegisterRequest;
import com.jobportal.backend.model.User;
import com.jobportal.backend.service.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private com.jobportal.backend.repository.UserRepository userRepository;

    @Autowired
    private com.jobportal.backend.repository.PasswordResetTokenRepository tokenRepository;

    @Autowired
    private UserService service;

    @PostMapping("/register")
    public org.springframework.http.ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        User user = new User();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPassword(req.getPassword());
        user.setRole(req.getRole());

        service.register(user);
        return org.springframework.http.ResponseEntity
                .ok(java.util.Collections.singletonMap("message", "User Registered Successfully"));
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest req) {
        return service.login(req.getEmail(), req.getPassword());
    }

    @PostMapping("/forgot-password")
    public org.springframework.http.ResponseEntity<?> forgotPassword(@RequestBody java.util.Map<String, String> req) {
        String email = req.get("email");
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return org.springframework.http.ResponseEntity.status(404).body("User not found");
        }

        String token = java.util.UUID.randomUUID().toString();
        com.jobportal.backend.model.PasswordResetToken resetToken = new com.jobportal.backend.model.PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUserId(user.getId());
        resetToken.setExpiryDate(java.time.LocalDateTime.now().plusHours(1));

        tokenRepository.deleteByUserId(user.getId()); // Clear old tokens
        tokenRepository.save(resetToken);

        // In a real app, send email here. For now, just log it.
        System.out.println("DEBUG: Password reset token for " + email + ": " + token);

        return org.springframework.http.ResponseEntity
                .ok(java.util.Collections.singletonMap("message", "Reset token generated"));
    }

    @PostMapping("/reset-password")
    public org.springframework.http.ResponseEntity<?> resetPassword(@RequestBody java.util.Map<String, String> req) {
        String token = req.get("token");
        String newPassword = req.get("newPassword");

        com.jobportal.backend.model.PasswordResetToken resetToken = tokenRepository.findByToken(token);
        if (resetToken == null || resetToken.getExpiryDate().isBefore(java.time.LocalDateTime.now())) {
            return org.springframework.http.ResponseEntity.status(400).body("Invalid or expired token");
        }

        service.updatePassword(resetToken.getUserId(), newPassword);
        tokenRepository.deleteByUserId(resetToken.getUserId());

        return org.springframework.http.ResponseEntity
                .ok(java.util.Collections.singletonMap("message", "Password reset successful"));
    }
}
