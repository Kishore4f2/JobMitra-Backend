package com.jobportal.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jobportal.backend.model.Application;
import com.jobportal.backend.model.Job;
import com.jobportal.backend.model.User;
import com.jobportal.backend.model.UserProfile;
import com.jobportal.backend.repository.ApplicationRepository;
import com.jobportal.backend.repository.JobRepository;
import com.jobportal.backend.repository.UserProfileRepository;
import com.jobportal.backend.repository.UserRepository;

/**
 * Application business logic.
 * Triggers professional SMTP emails (with PDF attachment) whenever
 * a recruiter explicitly accepts ("accepted") or rejects ("rejected")
 * a seeker's application.
 */
@Service
public class ApplicationService {

    @Autowired
    private ApplicationRepository repository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private EmailService emailService;

    // ── Submit application ─────────────────────────────────────────────

    public void apply(Application app) {
        if (app.getStatus() == null) {
            app.setStatus("pending");
        }
        repository.save(app);
    }

    // ── Retrieve applications ──────────────────────────────────────────

    public List<Application> getApplicationsBySeeker(int seekerId) {
        return repository.findBySeekerId(seekerId);
    }

    public List<Application> getApplicationsByJob(int jobId) {
        return repository.findByJobId(jobId);
    }

    // ── Update status & fire email ────────────────────────────────────

    /**
     * Updates the status of an application in the database, then —
     * if the new status is "accepted" or "rejected" — sends the
     * appropriate professional email with a PDF letter attached.
     */
    public void updateStatus(int applicationId, String newStatus) {
        // 1. Persist the new status
        repository.updateStatus(applicationId, newStatus);

        // 2. Only send email for terminal decisions
        String status = newStatus.toLowerCase().trim();
        if (!status.equals("accepted") && !status.equals("rejected")) {
            return;
        }

        try {
            // 3. Load the full application record (we need seekerId, jobId, etc.)
            Application application = repository.findById(applicationId);
            if (application == null) {
                System.err.println("[ApplicationService] Application not found for id=" + applicationId);
                return;
            }

            // 4. Load Job, Seeker User, and Seeker Profile
            Job job = jobRepository.findById(application.getJobId());
            User seeker = userRepository.findById(application.getSeekerId());
            UserProfile seekerProfile = userProfileRepository.findByUserId(application.getSeekerId());

            if (job == null || seeker == null) {
                System.err.println("[ApplicationService] Cannot send email — job or seeker not found.");
                return;
            }

            // 5. Dispatch the email asynchronously
            if (status.equals("accepted")) {
                emailService.sendAcceptanceEmail(application, job, seeker, seekerProfile);
            } else {
                emailService.sendRejectionEmail(application, job, seeker, seekerProfile);
            }

        } catch (Exception e) {
            // Log and continue — don't let an email error break the status update
            System.err.println("[ApplicationService] Email dispatch error: " + e.getMessage());
        }
    }
}
