package com.jobportal.backend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.jobportal.backend.model.SavedJob;
import com.jobportal.backend.repository.SavedJobRepository;

@RestController
@RequestMapping("/api/saved-jobs")
@CrossOrigin
public class SavedJobController {

    @Autowired
    private SavedJobRepository savedJobRepository;

    // ⭐ SAVE A JOB (Bookmark)
    @PostMapping("/{jobId}")
    public Map<String, Object> saveJob(@PathVariable int jobId,
            @RequestParam int seekerId) {
        Map<String, Object> response = new HashMap<>();
        if (savedJobRepository.exists(seekerId, jobId)) {
            response.put("message", "Job already saved");
            response.put("saved", true);
        } else {
            savedJobRepository.save(seekerId, jobId);
            response.put("message", "Job saved successfully");
            response.put("saved", true);
        }
        return response;
    }

    // ⭐ UNSAVE / REMOVE BOOKMARK
    @DeleteMapping("/{jobId}")
    public Map<String, Object> unsaveJob(@PathVariable int jobId,
            @RequestParam int seekerId) {
        savedJobRepository.delete(seekerId, jobId);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Job removed from saved list");
        response.put("saved", false);
        return response;
    }

    // ⭐ GET ALL SAVED JOBS FOR A SEEKER
    @GetMapping("/{seekerId}")
    public List<SavedJob> getSavedJobs(@PathVariable int seekerId) {
        return savedJobRepository.findBySeekerId(seekerId);
    }

    // ⭐ CHECK IF JOB IS SAVED
    @GetMapping("/{seekerId}/check/{jobId}")
    public Map<String, Boolean> isSaved(@PathVariable int seekerId,
            @PathVariable int jobId) {
        Map<String, Boolean> response = new HashMap<>();
        response.put("saved", savedJobRepository.exists(seekerId, jobId));
        return response;
    }
}
