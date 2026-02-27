package com.jobportal.backend.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.jobportal.backend.model.Application;
import com.jobportal.backend.model.Job;
import com.jobportal.backend.service.ApplicationService;
import com.jobportal.backend.service.JobService;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin
public class ApplicationController {

    @Autowired
    private ApplicationService service;

    @Autowired
    private JobService jobService;

    @Autowired
    private com.jobportal.backend.service.StorageService storageService;

    @PostMapping("/apply")
    public String apply(@RequestParam("jobId") int jobId,
            @RequestParam("seekerId") int seekerId,
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("resume") MultipartFile file) {
        try {
            // Check deadline
            Job job = jobService.getJobById(jobId);
            if (job != null && job.getDeadline() != null && !job.getDeadline().isEmpty()) {
                try {
                    java.time.LocalDate deadlineDate = null;
                    String deadlineStr = job.getDeadline();

                    // Support both YYYY-MM-DD and MM/DD/YYYY
                    if (deadlineStr.contains("-")) {
                        deadlineDate = java.time.LocalDate.parse(deadlineStr);
                    } else if (deadlineStr.contains("/")) {
                        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter
                                .ofPattern("MM/dd/yyyy");
                        deadlineDate = java.time.LocalDate.parse(deadlineStr, formatter);
                    }

                    if (deadlineDate != null && java.time.LocalDate.now().isAfter(deadlineDate)) {
                        return "Application deadline has passed. Last date was " + job.getDeadline();
                    }
                } catch (Exception e) {
                    System.err.println("Warning: Could not parse deadline: " + job.getDeadline());
                }
            }

            if (file.isEmpty()) {
                return "Resume is required";
            }

            // Save file via StorageService
            String filename = storageService.saveFile(file, "resumes");

            Application app = new Application();
            app.setJobId(jobId);
            app.setSeekerId(seekerId);
            app.setSeekerName(name);
            app.setSeekerEmail(email);
            app.setResumeFile(filename);
            app.setStatus("pending");

            service.apply(app);
            return "Application Submitted Successfully";

        } catch (IOException e) {
            return "Could not store file: " + e.getMessage();
        }
    }

    @GetMapping("/seeker/{id}")
    public List<Application> getBySeeker(@PathVariable int id) {
        return service.getApplicationsBySeeker(id);
    }

    @GetMapping("/job/{id}")
    public List<Application> getByJob(@PathVariable int id) {
        return service.getApplicationsByJob(id);
    }

    @PutMapping("/{id}/status")
    public String updateStatus(@PathVariable int id, @RequestParam String status) {
        service.updateStatus(id, status);
        return "Status Updated to " + status;
    }

    @GetMapping("/resume/{filename}")
    public org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> getResume(
            @PathVariable String filename) {
        try {
            org.springframework.core.io.Resource resource = storageService.loadFile(filename, "resumes");
            return org.springframework.http.ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.notFound().build();
        }
    }
}
