package com.jobportal.backend.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.jobportal.backend.model.Job;
import com.jobportal.backend.service.JobService;

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin
public class JobController {

    @Autowired
    private JobService service;

    @Autowired
    private com.jobportal.backend.repository.UserRepository userRepository;

    // ⭐ CREATE JOB
    @PostMapping("/create")
    public String createJob(@RequestBody Job job) {
        System.out.println("JobController: Received request to create job: " + job.getTitle());
        System.out.println("Recruiter ID: " + job.getRecruiterId());

        // Check if recruiter exists
        if (userRepository.findAll().stream().noneMatch(u -> u.getId() == job.getRecruiterId())) {
            System.err.println("Error: Recruiter with ID " + job.getRecruiterId() + " does not exist!");
            return "Error: Invalid Recruiter ID. Please log out and log in again.";
        }

        service.createJob(job);
        return "Job Created Successfully";
    }

    // ⭐ GET ALL JOBS
    @GetMapping
    public List<Job> getAllJobs(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (page != null && size != null) {
            return service.getPaginatedJobs(page, size);
        }
        return service.getAllJobs();
    }

    // ⭐ SEARCH JOBS
    @GetMapping("/search")
    public List<Job> search(@RequestParam String title) {
        return service.searchJobs(title);
    }

    // ⭐ FILTER BY LOCATION
    @GetMapping("/location")
    public List<Job> byLocation(@RequestParam(required = false) String location) {
        if (location == null || location.isEmpty()) {
            return service.getAllJobs();
        }
        return service.jobsByLocation(location);
    }

    // ⭐ JOBS BY RECRUITER
    @GetMapping("/recruiter/{id}")
    public List<Job> byRecruiter(@PathVariable int id) {
        return service.jobsByRecruiter(id);
    }

    @GetMapping("/recruiter/stats/{id}")
    public java.util.Map<String, Long> getRecruiterStats(@PathVariable int id) {
        return service.getRecruiterStats(id);
    }
}
