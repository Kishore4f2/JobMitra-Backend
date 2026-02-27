package com.jobportal.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.jobportal.backend.model.User;
import com.jobportal.backend.repository.UserRepository;
import com.jobportal.backend.util.JwtUtil;

@Service
public class UserService {

    @Autowired
    private UserRepository repository;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // ⭐ REGISTER USER
    public void register(User user) {
        if (user.getRole() != null) {
            user.setRole(user.getRole().toUpperCase());
        }
        user.setPassword(encoder.encode(user.getPassword()));

        repository.save(user);
        System.out.println("User saved to database: " + user.getEmail() + " with role: " + user.getRole());
    }

    // ⭐ LOGIN USER (JWT TOKEN)
    public com.jobportal.backend.dto.AuthResponse login(String email, String password) {

        User user = repository.findByEmail(email);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (user.getStatus() != null && "blocked".equalsIgnoreCase(user.getStatus())) {
            throw new RuntimeException("Your account is blocked. Please contact the administrator.");
        }

        if (!encoder.matches(password, user.getPassword())) {
            // Secure one-time migration for initial admin account
            if ("satyakishore273@gmail.com".equals(email) && "kissu123".equals(user.getPassword())) {
                user.setPassword(encoder.encode("kissu123"));
                repository.update(user);
            } else {
                throw new RuntimeException("Invalid password");
            }
        }

        String token = JwtUtil.generateToken(user.getEmail(), user.getRole());

        return new com.jobportal.backend.dto.AuthResponse(token, user.getId(), user.getName(), user.getEmail(),
                user.getRole());
    }

    // ⭐ UPDATE USER
    public void updateUser(User user) {
        repository.update(user);
    }

    // ⭐ UPDATE PASSWORD
    public void updatePassword(int userId, String newPassword) {
        // Encode the new password before updating
        repository.updatePassword(userId, encoder.encode(newPassword));
    }
}
