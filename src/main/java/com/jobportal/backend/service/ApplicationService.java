package com.jobportal.backend.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.jobportal.backend.model.Application;
import com.jobportal.backend.repository.ApplicationRepository;

@Service
public class ApplicationService {

    @Autowired
    private ApplicationRepository repository;

    public void apply(Application app) {
        if (app.getStatus() == null)
            app.setStatus("pending");
        repository.save(app);
    }

    public List<Application> getApplicationsBySeeker(int seekerId) {
        return repository.findBySeekerId(seekerId);
    }

    public List<Application> getApplicationsByJob(int jobId) {
        return repository.findByJobId(jobId);
    }

    public void updateStatus(int id, String status) {
        repository.updateStatus(id, status);
    }
}
