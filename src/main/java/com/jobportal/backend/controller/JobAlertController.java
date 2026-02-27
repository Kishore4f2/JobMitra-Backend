package com.jobportal.backend.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.jobportal.backend.model.JobAlert;
import com.jobportal.backend.repository.JobAlertRepository;

@RestController
@RequestMapping("/api/alerts")
@CrossOrigin(origins = "http://localhost:5173")
public class JobAlertController {

    @Autowired
    private JobAlertRepository jobAlertRepository;

    @PostMapping
    public org.springframework.http.ResponseEntity<?> createAlert(@RequestBody JobAlert alert) {
        try {
            jobAlertRepository.save(alert);
            return org.springframework.http.ResponseEntity
                    .ok(java.util.Map.of("message", "Job alert created successfully"));
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.status(500)
                    .body(java.util.Map.of("message", "Error creating job alert: " + e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public org.springframework.http.ResponseEntity<List<JobAlert>> getAlerts(@PathVariable int userId) {
        return org.springframework.http.ResponseEntity.ok(jobAlertRepository.findByUserId(userId));
    }

    @DeleteMapping("/{id}")
    public org.springframework.http.ResponseEntity<?> deleteAlert(@PathVariable int id, @RequestParam int userId) {
        int rows = jobAlertRepository.deleteById(id, userId);
        if (rows > 0) {
            return org.springframework.http.ResponseEntity.ok(java.util.Map.of("message", "Job alert deleted"));
        }
        return org.springframework.http.ResponseEntity.status(404)
                .body(java.util.Map.of("message", "Alert not found or unauthorized"));
    }
}
