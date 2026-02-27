package com.jobportal.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jobportal.backend.model.Job;
import com.jobportal.backend.repository.JobRepository;

@Service
public class JobService {

    @Autowired
    private JobRepository repository;

    public void createJob(Job job) {
        repository.save(job);
    }

    public List<Job> getAllJobs() {
        return repository.findAll();
    }

    public List<Job> getPaginatedJobs(int page, int size) {
        return repository.findPaginated(page, size);
    }

    public List<Job> searchJobs(String title) {
        return repository.searchByTitle(title);
    }

    public List<Job> jobsByLocation(String location) {
        return repository.findByLocation(location);
    }

    public List<Job> jobsByRecruiter(int recruiterId) {
        return repository.findByRecruiter(recruiterId);
    }

    public Job getJobById(int jobId) {
        return repository.findById(jobId);
    }

    public java.util.Map<String, Long> getRecruiterStats(int recruiterId) {
        return repository.getRecruiterStats(recruiterId);
    }
}
