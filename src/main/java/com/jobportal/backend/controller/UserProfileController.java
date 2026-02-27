package com.jobportal.backend.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.jobportal.backend.model.User;
import com.jobportal.backend.model.UserProfile;
import com.jobportal.backend.repository.UserProfileRepository;
import com.jobportal.backend.repository.UserRepository;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin
public class UserProfileController {

    @Autowired
    private UserProfileRepository profileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.jobportal.backend.service.StorageService storageService;

    // ⭐ GET PROFILE BY USER ID
    @GetMapping("/{userId}")
    public ResponseEntity<?> getProfile(@PathVariable int userId) {
        try {
            UserProfile profile = profileRepository.findByUserId(userId);
            if (profile == null) {
                // Return empty profile for new users
                UserProfile empty = new UserProfile();
                empty.setUserId(userId);
                return ResponseEntity.ok(empty);
            }
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Error finding profile"));
        }
    }

    // ⭐ UPDATE PROFILE (name, skills, bio, experience, location, linkedin)
    @PutMapping("/{userId}")
    public ResponseEntity<?> updateProfile(@PathVariable int userId,
            @RequestBody Map<String, String> body) {
        try {
            // Update name in users table if provided
            if (body.containsKey("name")) {
                User user = userRepository.findById(userId);
                if (user != null) {
                    user.setName(body.get("name"));
                    userRepository.update(user);
                }
            }

            // Upsert into user_profiles table
            UserProfile profile = new UserProfile();
            profile.setUserId(userId);
            profile.setSkills(body.getOrDefault("skills", ""));
            profile.setBio(body.getOrDefault("bio", ""));
            profile.setExperience(body.getOrDefault("experience", ""));
            profile.setLocation(body.getOrDefault("location", ""));
            profile.setLinkedinUrl(body.getOrDefault("linkedinUrl", ""));

            profileRepository.save(profile);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Profile updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Error updating profile"));
        }
    }

    // ⭐ UPLOAD PROFILE PHOTO
    @PostMapping("/{userId}/photo")
    public ResponseEntity<?> uploadPhoto(@PathVariable int userId,
            @RequestParam("photo") MultipartFile file) {
        try {
            String filename = storageService.saveFile(file, "photos");
            String photoUrl = "/api/profile/photos/" + filename;
            profileRepository.updatePhotoUrl(userId, photoUrl);

            Map<String, String> response = new HashMap<>();
            response.put("photoUrl", photoUrl);
            response.put("message", "Photo uploaded successfully");
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Could not upload photo: " + e.getMessage()));
        }
    }

    // ⭐ SERVE PROFILE PHOTO
    @GetMapping("/photos/{filename}")
    public ResponseEntity<org.springframework.core.io.Resource> getPhoto(@PathVariable String filename) {
        try {
            org.springframework.core.io.Resource resource = storageService.loadFile(filename, "photos");

            // Detect content type from file extension
            org.springframework.http.MediaType mediaType = org.springframework.http.MediaType.IMAGE_JPEG;
            String lowerName = filename.toLowerCase();
            if (lowerName.endsWith(".png")) {
                mediaType = org.springframework.http.MediaType.IMAGE_PNG;
            } else if (lowerName.endsWith(".gif")) {
                mediaType = org.springframework.http.MediaType.IMAGE_GIF;
            } else if (lowerName.endsWith(".webp")) {
                mediaType = org.springframework.http.MediaType.parseMediaType("image/webp");
            }

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(org.springframework.http.HttpHeaders.CACHE_CONTROL, "max-age=3600")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
