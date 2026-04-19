package com.jobportal.backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.jobportal.backend.model.Application;
import com.jobportal.backend.model.Job;
import com.jobportal.backend.model.User;
import com.jobportal.backend.model.UserProfile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Sends ultra-professional HTML emails with PDF attachments via SMTP.
 * Triggered when a recruiter accepts or rejects a job seeker's application.
 */
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PdfGeneratorService pdfGeneratorService;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // ─────────────────────────────────────────────────────────────────
    //  PUBLIC API
    // ─────────────────────────────────────────────────────────────────

    /**
     * Sends a professional acceptance email with PDF letter to the job seeker.
     * Runs asynchronously so the API call returns immediately.
     */
    @Async
    public void sendAcceptanceEmail(Application application, Job job, User seeker, UserProfile seekerProfile) {
        try {
            byte[] pdfBytes = pdfGeneratorService.generateAcceptanceLetter(application, job, seeker, seekerProfile);
            String subject  = buildAcceptanceSubject(job);
            String html     = buildAcceptanceHtml(seeker, job, application);
            sendEmailWithPdfAttachment(seeker.getEmail(), subject, html, pdfBytes,
                    "JobMitra_Acceptance_Letter_" + application.getId() + ".pdf");
        } catch (Exception e) {
            System.err.println("[EmailService] Failed to send acceptance email: " + e.getMessage());
        }
    }

    /**
     * Sends a professional rejection email with PDF letter to the job seeker.
     * Runs asynchronously.
     */
    @Async
    public void sendRejectionEmail(Application application, Job job, User seeker, UserProfile seekerProfile) {
        try {
            byte[] pdfBytes = pdfGeneratorService.generateRejectionLetter(application, job, seeker, seekerProfile);
            String subject  = buildRejectionSubject(job);
            String html     = buildRejectionHtml(seeker, job, application);
            sendEmailWithPdfAttachment(seeker.getEmail(), subject, html, pdfBytes,
                    "JobMitra_Application_Status_" + application.getId() + ".pdf");
        } catch (Exception e) {
            System.err.println("[EmailService] Failed to send rejection email: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  CORE MAIL SENDER
    // ─────────────────────────────────────────────────────────────────

    private void sendEmailWithPdfAttachment(String to, String subject, String htmlBody,
                                            byte[] pdfBytes, String pdfFileName)
            throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        try {
            helper.setFrom(fromEmail, "JobMitra - Career Platform");
        } catch (java.io.UnsupportedEncodingException e) {
            helper.setFrom(fromEmail);
        }
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true); // true = isHtml

        // Attach PDF
        helper.addAttachment(pdfFileName,
                new org.springframework.core.io.ByteArrayResource(pdfBytes),
                "application/pdf");

        mailSender.send(message);
        System.out.println("[EmailService] Email sent to " + to + " | Subject: " + subject);
    }

    // -----------------------------------------------------------------
    //  SUBJECT BUILDERS
    // -----------------------------------------------------------------

    private String buildAcceptanceSubject(Job job) {
        return "Congratulations! Your Application for '" + safe(job.getTitle())
                + "' at " + safe(job.getCompany()) + " has been Accepted - JobMitra";
    }

    private String buildRejectionSubject(Job job) {
        return "Application Update: " + safe(job.getTitle())
                + " at " + safe(job.getCompany()) + " - JobMitra";
    }

    // ─────────────────────────────────────────────────────────────────
    //  HTML EMAIL BUILDERS
    // ─────────────────────────────────────────────────────────────────

    private String buildAcceptanceHtml(User seeker, Job job, Application application) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        String ref   = "JM-" + String.format("%06d", application.getId());

        return "<!DOCTYPE html>" +
            "<html lang=\"en\">" +
            "<head>" +
            "<meta charset=\"UTF-8\"/>" +
            "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"/>" +
            "<title>Application Accepted — JobMitra</title>" +
            "</head>" +
            "<body style=\"margin:0;padding:0;background:#f0f0f5;font-family:'Segoe UI',Arial,sans-serif;\">" +

            // WRAPPER
            "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#f0f0f5;padding:40px 0;\">" +
            "<tr><td align=\"center\">" +

            // EMAIL CARD
            "<table width=\"600\" cellpadding=\"0\" cellspacing=\"0\" style=\"max-width:600px;width:100%;border-radius:16px;overflow:hidden;box-shadow:0 8px 40px rgba(0,0,0,0.12);\">" +

            // ── HEADER ──────────────────────────────────────────────
            "<tr><td style=\"background:linear-gradient(135deg,#4E2FA8 0%,#3D4ECF 100%);padding:0;\">" +
            "  <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\">" +
            "    <tr><td style=\"background:#F3BC43;height:4px;\"></td></tr>" +
            "    <tr><td style=\"padding:32px 40px 28px;\">" +
            "      <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\">" +
            "        <tr>" +
            "          <td>" +
            "            <div style=\"font-size:28px;font-weight:800;color:#ffffff;letter-spacing:-0.5px;\">JobMitra</div>" +
            "            <div style=\"font-size:11px;color:rgba(255,255,255,0.65);margin-top:4px;font-style:italic;\">Find Your Way — India's Trusted Job Platform</div>" +
            "          </td>" +
            "          <td align=\"right\" style=\"vertical-align:top;\">" +
            "            <div style=\"font-size:10px;color:rgba(255,255,255,0.6);\">" + today + "</div>" +
            "            <div style=\"font-size:10px;color:rgba(255,255,255,0.5);margin-top:2px;\">Ref: " + ref + "</div>" +
            "          </td>" +
            "        </tr>" +
            "      </table>" +
            "    </td></tr>" +
            "    <tr><td style=\"padding:0 40px 32px;\">" +
            "      <div style=\"display:inline-block;background:rgba(34,178,115,0.9);border-radius:50px;padding:8px 24px;\">" +
            "        <span style=\"color:#ffffff;font-size:13px;font-weight:700;letter-spacing:0.5px;\">✓ &nbsp; APPLICATION ACCEPTED</span>" +
            "      </div>" +
            "    </td></tr>" +
            "  </table>" +
            "</td></tr>" +

            // ── BODY ────────────────────────────────────────────────
            "<tr><td style=\"background:#ffffff;padding:40px;\">" +

            // Greeting
            "<p style=\"font-size:18px;font-weight:700;color:#1A1A24;margin:0 0 6px;\">Dear " + safe(seeker.getName()) + ",</p>" +
            "<p style=\"font-size:14px;color:#666;line-height:1.6;margin:0 0 28px;\">" +
            "We are thrilled to inform you that after a thorough review of your profile, " +
            "<strong style=\"color:#4E2FA8;\">" + safe(job.getCompany()) + "</strong> has" +
            " decided to move forward with your application for the position listed below." +
            "</p>" +

            // Highlight box
            "<div style=\"background:linear-gradient(135deg,#f5f0ff 0%,#eef1ff 100%);border-left:4px solid #4E2FA8;border-radius:8px;padding:20px 24px;margin-bottom:28px;\">" +
            "  <div style=\"font-size:10px;color:#8888a0;font-weight:700;letter-spacing:1px;margin-bottom:10px;\">POSITION DETAILS</div>" +
            "  <div style=\"font-size:18px;font-weight:800;color:#4E2FA8;margin-bottom:4px;\">" + safe(job.getTitle()) + "</div>" +
            "  <div style=\"font-size:13px;color:#444;margin-bottom:8px;\">" + safe(job.getCompany()) + " &bull; " + safe(job.getLocation()) + "</div>" +
            "  <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\">" +
            "    <tr>" +
            "      <td style=\"padding:4px 0;\">" + pill("#22B273", safe(job.getJobType())) + "</td>" +
            "      <td style=\"padding:4px 0;\">" + pill("#3D4ECF", safe(job.getSalaryRange())) + "</td>" +
            "      <td style=\"padding:4px 0;\">" + pill("#7c3aed", safe(job.getExperience())) + "</td>" +
            "    </tr>" +
            "  </table>" +
            "</div>" +

            // What next
            "<div style=\"background:#f8f8fc;border-radius:8px;padding:20px 24px;margin-bottom:28px;\">" +
            "  <div style=\"font-size:10px;color:#8888a0;font-weight:700;letter-spacing:1px;margin-bottom:12px;\">NEXT STEPS</div>" +
            "  " + step("1", "#4E2FA8", "Our HR team will contact you shortly with interview details.") +
            "  " + step("2", "#3D4ECF", "Please keep your phone and inbox available for further communication.") +
            "  " + step("3", "#22B273", "Review the attached PDF acceptance letter for full details.") +
            "</div>" +

            // Closing
            "<p style=\"font-size:14px;color:#555;line-height:1.7;margin:0 0 28px;\">" +
            "We are confident you will be an excellent addition to the team. " +
            "Once again, congratulations on this achievement! 🎉" +
            "</p>" +

            // CTA Button
            "<div style=\"text-align:center;margin-bottom:32px;\">" +
            "  <a href=\"https://jobmitra-find-your-way.vercel.app\" " +
            "     style=\"display:inline-block;background:linear-gradient(135deg,#4E2FA8,#3D4ECF);" +
            "     color:#fff;text-decoration:none;border-radius:50px;padding:14px 40px;" +
            "     font-size:14px;font-weight:700;letter-spacing:0.3px;box-shadow:0 4px 16px rgba(78,47,168,0.35);\">" +
            "    Visit JobMitra Dashboard &rarr;" +
            "  </a>" +
            "</div>" +

            // Signature
            "<div style=\"border-top:1px solid #eee;padding-top:20px;\">" +
            "  <p style=\"font-size:13px;color:#555;margin:0;\">Warm regards,</p>" +
            "  <p style=\"font-size:14px;font-weight:700;color:#1A1A24;margin:6px 0 2px;\">" +
                 safe(job.getRecruiterName() != null ? job.getRecruiterName() : "Recruitment Team") + "</p>" +
            "  <p style=\"font-size:12px;color:#888;margin:0;\">" + safe(job.getCompany()) + " &bull; via JobMitra</p>" +
            "</div>" +

            "</td></tr>" +

            // ── FOOTER ──────────────────────────────────────────────
            "<tr><td style=\"background:linear-gradient(135deg,#3D4ECF 0%,#4E2FA8 100%);padding:20px 40px;text-align:center;\">" +
            "  <p style=\"font-size:11px;color:rgba(255,255,255,0.6);margin:0 0 6px;\">" +
            "    &copy; " + LocalDate.now().getYear() + " JobMitra. All Rights Reserved." +
            "  </p>" +
            "  <p style=\"font-size:10px;color:rgba(255,255,255,0.4);margin:0;\">" +
            "    This is an auto-generated email. Please do not reply directly." +
            "  </p>" +
            "</td></tr>" +

            "</table>" + // end card
            "</td></tr></table>" + // end wrapper
            "</body></html>";
    }

    private String buildRejectionHtml(User seeker, Job job, Application application) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        String ref   = "JM-" + String.format("%06d", application.getId());

        return "<!DOCTYPE html>" +
            "<html lang=\"en\">" +
            "<head>" +
            "<meta charset=\"UTF-8\"/>" +
            "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"/>" +
            "<title>Application Update — JobMitra</title>" +
            "</head>" +
            "<body style=\"margin:0;padding:0;background:#f0f0f5;font-family:'Segoe UI',Arial,sans-serif;\">" +

            "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#f0f0f5;padding:40px 0;\">" +
            "<tr><td align=\"center\">" +

            "<table width=\"600\" cellpadding=\"0\" cellspacing=\"0\" style=\"max-width:600px;width:100%;border-radius:16px;overflow:hidden;box-shadow:0 8px 40px rgba(0,0,0,0.12);\">" +

            // ── HEADER ──────────────────────────────────────────────
            "<tr><td style=\"background:linear-gradient(135deg,#4E2FA8 0%,#3D4ECF 100%);padding:0;\">" +
            "  <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\">" +
            "    <tr><td style=\"background:#F3BC43;height:4px;\"></td></tr>" +
            "    <tr><td style=\"padding:32px 40px 28px;\">" +
            "      <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\">" +
            "        <tr>" +
            "          <td>" +
            "            <div style=\"font-size:28px;font-weight:800;color:#ffffff;letter-spacing:-0.5px;\">JobMitra</div>" +
            "            <div style=\"font-size:11px;color:rgba(255,255,255,0.65);margin-top:4px;font-style:italic;\">Find Your Way — India's Trusted Job Platform</div>" +
            "          </td>" +
            "          <td align=\"right\" style=\"vertical-align:top;\">" +
            "            <div style=\"font-size:10px;color:rgba(255,255,255,0.6);\">" + today + "</div>" +
            "            <div style=\"font-size:10px;color:rgba(255,255,255,0.5);margin-top:2px;\">Ref: " + ref + "</div>" +
            "          </td>" +
            "        </tr>" +
            "      </table>" +
            "    </td></tr>" +
            "    <tr><td style=\"padding:0 40px 32px;\">" +
            "      <div style=\"display:inline-block;background:rgba(180,40,40,0.85);border-radius:50px;padding:8px 24px;\">" +
            "        <span style=\"color:#ffffff;font-size:13px;font-weight:700;letter-spacing:0.5px;\">✗ &nbsp; APPLICATION UPDATE</span>" +
            "      </div>" +
            "    </td></tr>" +
            "  </table>" +
            "</td></tr>" +

            // ── BODY ────────────────────────────────────────────────
            "<tr><td style=\"background:#ffffff;padding:40px;\">" +

            "<p style=\"font-size:18px;font-weight:700;color:#1A1A24;margin:0 0 6px;\">Dear " + safe(seeker.getName()) + ",</p>" +
            "<p style=\"font-size:14px;color:#666;line-height:1.6;margin:0 0 28px;\">" +
            "Thank you sincerely for your interest in the position below and for the time " +
            "you invested in your application. After a careful and thorough review of all " +
            "candidates, we regret to inform you that we will not be moving forward with " +
            "your application at this stage." +
            "</p>" +

            // Job Details
            "<div style=\"background:#fafafa;border-left:4px solid #D93C3C;border-radius:8px;padding:20px 24px;margin-bottom:28px;\">" +
            "  <div style=\"font-size:10px;color:#8888a0;font-weight:700;letter-spacing:1px;margin-bottom:10px;\">POSITION APPLIED FOR</div>" +
            "  <div style=\"font-size:18px;font-weight:800;color:#2a2a3a;margin-bottom:4px;\">" + safe(job.getTitle()) + "</div>" +
            "  <div style=\"font-size:13px;color:#444;\">" + safe(job.getCompany()) + " &bull; " + safe(job.getLocation()) + "</div>" +
            "</div>" +

            // Encouragement
            "<div style=\"background:#f5f8ff;border-radius:8px;padding:20px 24px;margin-bottom:28px;\">" +
            "  <div style=\"font-size:10px;color:#8888a0;font-weight:700;letter-spacing:1px;margin-bottom:12px;\">KEEP MOVING FORWARD</div>" +
            "  " + step("💡", "#4E2FA8", "Every rejection brings you one step closer to the right opportunity.") +
            "  " + step("📚", "#3D4ECF", "Keep upskilling — explore certifications and new technologies.") +
            "  " + step("🔍", "#22B273", "Browse hundreds of open positions on JobMitra that match your profile.") +
            "</div>" +

            "<p style=\"font-size:14px;color:#555;line-height:1.7;margin:0 0 28px;\">" +
            "This decision was purely based on the current requirements of the role and is " +
            "not a reflection of your overall capabilities or potential. We encourage you to " +
            "explore other opportunities and apply again in the future." +
            "</p>" +

            // CTA
            "<div style=\"text-align:center;margin-bottom:32px;\">" +
            "  <a href=\"https://jobmitra-find-your-way.vercel.app\" " +
            "     style=\"display:inline-block;background:linear-gradient(135deg,#4E2FA8,#3D4ECF);" +
            "     color:#fff;text-decoration:none;border-radius:50px;padding:14px 40px;" +
            "     font-size:14px;font-weight:700;letter-spacing:0.3px;box-shadow:0 4px 16px rgba(78,47,168,0.35);\">" +
            "    Explore More Jobs on JobMitra &rarr;" +
            "  </a>" +
            "</div>" +

            // Signature
            "<div style=\"border-top:1px solid #eee;padding-top:20px;\">" +
            "  <p style=\"font-size:13px;color:#555;margin:0;\">Warm regards,</p>" +
            "  <p style=\"font-size:14px;font-weight:700;color:#1A1A24;margin:6px 0 2px;\">" +
             safe(job.getRecruiterName() != null ? job.getRecruiterName() : "Recruitment Team") + "</p>" +
            "  <p style=\"font-size:12px;color:#888;margin:0;\">" + safe(job.getCompany()) + " &bull; via JobMitra</p>" +
            "</div>" +

            "</td></tr>" +

            // ── FOOTER ──────────────────────────────────────────────
            "<tr><td style=\"background:linear-gradient(135deg,#3D4ECF 0%,#4E2FA8 100%);padding:20px 40px;text-align:center;\">" +
            "  <p style=\"font-size:11px;color:rgba(255,255,255,0.6);margin:0 0 6px;\">" +
            "    &copy; " + LocalDate.now().getYear() + " JobMitra. All Rights Reserved." +
            "  </p>" +
            "  <p style=\"font-size:10px;color:rgba(255,255,255,0.4);margin:0;\">" +
            "    This is an auto-generated email. Please do not reply directly." +
            "  </p>" +
            "</td></tr>" +

            "</table>" +
            "</td></tr></table>" +
            "</body></html>";
    }

    // ─────────────────────────────────────────────────────────────────
    //  HTML COMPONENT HELPERS
    // ─────────────────────────────────────────────────────────────────

    /** Renders a small coloured badge/pill */
    private String pill(String color, String text) {
        return "<span style=\"display:inline-block;background:" + color + ";color:#fff;" +
               "border-radius:50px;padding:3px 12px;font-size:11px;font-weight:600;" +
               "margin-right:6px;\">" + safe(text) + "</span>";
    }

    /** Renders a numbered or emoji step row */
    private String step(String marker, String color, String text) {
        return "<table cellpadding=\"0\" cellspacing=\"0\" style=\"margin-bottom:10px;width:100%;\">" +
               "<tr>" +
               "  <td style=\"width:32px;vertical-align:top;\">" +
               "    <div style=\"width:26px;height:26px;border-radius:50%;background:" + color + ";" +
               "         color:#fff;font-size:12px;font-weight:700;text-align:center;line-height:26px;\">" +
               marker + "</div>" +
               "  </td>" +
               "  <td style=\"vertical-align:middle;padding-left:8px;font-size:13px;color:#444;line-height:1.5;\">" +
               text + "</td>" +
               "</tr></table>";
    }

    // ─────────────────────────────────────────────────────────────────
    //  UTILITIES
    // ─────────────────────────────────────────────────────────────────

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "N/A" : s.trim();
    }
}
