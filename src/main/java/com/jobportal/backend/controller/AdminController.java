package com.jobportal.backend.controller;

import com.jobportal.backend.model.User;
import com.jobportal.backend.model.Job;
import com.jobportal.backend.model.Application;
import com.jobportal.backend.repository.UserRepository;
import com.jobportal.backend.repository.JobRepository;
import com.jobportal.backend.repository.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        List<User> users = userRepository.findAll();
        List<Job> jobs = jobRepository.findAll();
        List<Application> apps = applicationRepository.findAll();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", users.size());
        stats.put("totalRecruiters", users.stream().filter(u -> "RECRUITER".equalsIgnoreCase(u.getRole())).count());
        stats.put("totalSeekers", users.stream().filter(u -> "SEEKER".equalsIgnoreCase(u.getRole())).count());
        stats.put("totalJobs", jobs.size());
        stats.put("totalApplications", apps.size());
        stats.put("blockedUsers", users.stream().filter(u -> "blocked".equalsIgnoreCase(u.getStatus())).count());

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/jobs")
    public ResponseEntity<List<Job>> getAllJobs() {
        return ResponseEntity.ok(jobRepository.findAll());
    }

    @DeleteMapping("/jobs/{id}")
    public ResponseEntity<?> deleteJob(@PathVariable int id) {
        jobRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/applications")
    public ResponseEntity<List<Application>> getAllApplications() {
        return ResponseEntity.ok(applicationRepository.findAll());
    }

    @DeleteMapping("/applications/{id}")
    public ResponseEntity<?> deleteApplication(@PathVariable int id) {
        applicationRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<?> updateUserStatus(@PathVariable int id, @RequestBody Map<String, String> payload) {
        User user = null;
        List<User> users = userRepository.findAll();
        for (User u : users) {
            if (u.getId() == id) {
                user = u;
                break;
            }
        }

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        user.setStatus(payload.get("status"));
        userRepository.update(user);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable int id, @RequestBody Map<String, String> payload) {
        User user = null;
        List<User> users = userRepository.findAll();
        for (User u : users) {
            if (u.getId() == id) {
                user = u;
                break;
            }
        }

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        user.setRole(payload.get("role").toUpperCase());
        userRepository.update(user);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable int id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
