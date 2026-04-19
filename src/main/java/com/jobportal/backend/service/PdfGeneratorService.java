package com.jobportal.backend.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.springframework.stereotype.Service;

import com.jobportal.backend.model.Application;
import com.jobportal.backend.model.Job;
import com.jobportal.backend.model.User;
import com.jobportal.backend.model.UserProfile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Generates ultra-professional PDF letters (acceptance / rejection) for JobMitra.
 * Uses Apache PDFBox 3.x — no licensing restrictions.
 */
@Service
public class PdfGeneratorService {

    // ── Brand Colours ────────────────────────────────────────────────
    private static final float[] BRAND_PURPLE   = {0.306f, 0.184f, 0.659f}; // #4E2FA8
    private static final float[] BRAND_INDIGO   = {0.239f, 0.306f, 0.812f}; // #3D4ECF
    private static final float[] ACCENT_GOLD    = {0.953f, 0.737f, 0.263f}; // #F3BC43
    private static final float[] SUCCESS_GREEN  = {0.133f, 0.698f, 0.451f}; // #22B273
    private static final float[] DANGER_RED     = {0.851f, 0.235f, 0.235f}; // #D93C3C
    private static final float[] LIGHT_GRAY     = {0.965f, 0.965f, 0.973f}; // #F6F6F8
    private static final float[] MED_GRAY       = {0.502f, 0.502f, 0.541f}; // #808089
    private static final float[] DARK           = {0.102f, 0.102f, 0.141f}; // #1A1A24
    private static final float[] WHITE          = {1.0f, 1.0f, 1.0f};

    // ── Fonts (Standard 14 — always available, no file loading) ──────
    private PDType1Font fontBold()   { return new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);   }
    private PDType1Font fontNormal() { return new PDType1Font(Standard14Fonts.FontName.HELVETICA);        }
    private PDType1Font fontOblique(){ return new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);}

    // ─────────────────────────────────────────────────────────────────
    //  PUBLIC API
    // ─────────────────────────────────────────────────────────────────

    /**
     * Generates an acceptance letter PDF.
     */
    public byte[] generateAcceptanceLetter(Application application, Job job, User seeker, UserProfile seekerProfile) {
        return buildPdf(application, job, seeker, seekerProfile, true);
    }

    /**
     * Generates a rejection letter PDF.
     */
    public byte[] generateRejectionLetter(Application application, Job job, User seeker, UserProfile seekerProfile) {
        return buildPdf(application, job, seeker, seekerProfile, false);
    }

    // ─────────────────────────────────────────────────────────────────
    //  CORE PDF BUILDER
    // ─────────────────────────────────────────────────────────────────

    private byte[] buildPdf(Application application, Job job, User seeker,
                            UserProfile seekerProfile, boolean accepted) {

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            float pageW = page.getMediaBox().getWidth();   // 595 pts
            float pageH = page.getMediaBox().getHeight();  // 842 pts
            float margin = 48f;
            float contentW = pageW - 2 * margin;

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {

                // ── 1. Background ────────────────────────────────────
                drawRect(cs, 0, 0, pageW, pageH, LIGHT_GRAY);

                // ── 2. Header gradient band ───────────────────────────
                drawRect(cs, 0, pageH - 120, pageW, 120, BRAND_PURPLE);
                // Accent strip at the very top
                drawRect(cs, 0, pageH - 4, pageW, 4, ACCENT_GOLD);

                // ── 3. Header — Logo text + Tagline ───────────────────
                // "JobMitra" wordmark
                setFillColor(cs, WHITE);
                cs.beginText();
                cs.setFont(fontBold(), 30);
                cs.newLineAtOffset(margin, pageH - 70);
                cs.showText("JobMitra");
                cs.endText();

                // Tagline
                setFillColor(cs, new float[]{0.85f, 0.78f, 1.0f});
                cs.beginText();
                cs.setFont(fontOblique(), 10);
                cs.newLineAtOffset(margin, pageH - 86);
                cs.showText("Find Your Way — India's Trusted Job Platform");
                cs.endText();

                // Date top-right
                String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
                setFillColor(cs, new float[]{0.85f, 0.78f, 1.0f});
                cs.beginText();
                cs.setFont(fontNormal(), 9);
                cs.newLineAtOffset(pageW - margin - 100, pageH - 70);
                cs.showText("Date: " + today);
                cs.endText();

                // Ref number
                cs.beginText();
                cs.setFont(fontNormal(), 9);
                cs.newLineAtOffset(pageW - margin - 100, pageH - 82);
                cs.showText("Ref: JM-" + String.format("%06d", application.getId()));
                cs.endText();

                // ── 4. Status Badge ───────────────────────────────────
                float badgeY = pageH - 145;
                float badgeH = 28f;
                float[] badgeColor = accepted ? SUCCESS_GREEN : DANGER_RED;
                String badgeText = accepted ? "✓  APPLICATION ACCEPTED" : "✗  APPLICATION DECLINED";

                drawRoundRect(cs, margin, badgeY, contentW, badgeH, badgeColor);

                setFillColor(cs, WHITE);
                cs.beginText();
                cs.setFont(fontBold(), 11);
                cs.newLineAtOffset(margin + (contentW / 2f) - 85, badgeY + 9);
                cs.showText(badgeText);
                cs.endText();

                // ── 5. White content card ─────────────────────────────
                float cardTopY   = badgeY - 14;
                float cardHeight = cardTopY - margin - 10;
                drawRect(cs, margin, margin, contentW, cardHeight, WHITE);

                // Card top accent line
                float[] accentC = accepted ? SUCCESS_GREEN : DANGER_RED;
                drawRect(cs, margin, cardTopY - 3, contentW, 3, accentC);

                // ── 6. Greeting ───────────────────────────────────────
                float y = cardTopY - 35;
                setFillColor(cs, DARK);
                cs.beginText();
                cs.setFont(fontBold(), 13);
                cs.newLineAtOffset(margin + 20, y);
                cs.showText("Dear " + safe(seeker.getName()) + ",");
                cs.endText();

                y -= 22;
                setFillColor(cs, MED_GRAY);
                if (accepted) {
                    cs.beginText();
                    cs.setFont(fontNormal(), 10);
                    cs.newLineAtOffset(margin + 20, y);
                    cs.showText("We are delighted to inform you that your application has been reviewed and");
                    cs.endText();
                    y -= 14;
                    cs.beginText();
                    cs.setFont(fontNormal(), 10);
                    cs.newLineAtOffset(margin + 20, y);
                    cs.showText("we would like to move forward with your candidacy for the position below.");
                    cs.endText();
                } else {
                    cs.beginText();
                    cs.setFont(fontNormal(), 10);
                    cs.newLineAtOffset(margin + 20, y);
                    cs.showText("Thank you for your interest and the time you invested in applying. After careful");
                    cs.endText();
                    y -= 14;
                    cs.beginText();
                    cs.setFont(fontNormal(), 10);
                    cs.newLineAtOffset(margin + 20, y);
                    cs.showText("consideration, we will not be moving forward with your application at this time.");
                    cs.endText();
                }

                // ── 7. Divider ────────────────────────────────────────
                y -= 20;
                drawRect(cs, margin + 20, y, contentW - 40, 1, new float[]{0.88f, 0.88f, 0.92f});

                // ── 8. Section: Position Details ──────────────────────
                y -= 18;
                drawSectionHeader(cs, margin + 20, y, "POSITION DETAILS", BRAND_PURPLE);

                y -= 22;
                y = drawLabelValue(cs, margin + 20, y, contentW - 40, "Job Title",       safe(job.getTitle()));
                y = drawLabelValue(cs, margin + 20, y, contentW - 40, "Company",         safe(job.getCompany()));
                y = drawLabelValue(cs, margin + 20, y, contentW - 40, "Location",        safe(job.getLocation()));
                y = drawLabelValue(cs, margin + 20, y, contentW - 40, "Job Type",        safe(job.getJobType()));
                y = drawLabelValue(cs, margin + 20, y, contentW - 40, "Salary Range",    safe(job.getSalaryRange()));
                y = drawLabelValue(cs, margin + 20, y, contentW - 40, "Experience Req.", safe(job.getExperience()));
                if (job.getSkills() != null && !job.getSkills().isEmpty()) {
                    y = drawLabelValue(cs, margin + 20, y, contentW - 40, "Required Skills",
                            String.join(", ", job.getSkills()));
                }

                // ── 9. Divider ────────────────────────────────────────
                y -= 10;
                drawRect(cs, margin + 20, y, contentW - 40, 1, new float[]{0.88f, 0.88f, 0.92f});

                // ── 10. Section: Applicant Details ────────────────────
                y -= 18;
                drawSectionHeader(cs, margin + 20, y, "APPLICANT DETAILS", BRAND_PURPLE);

                y -= 22;
                y = drawLabelValue(cs, margin + 20, y, contentW - 40, "Full Name",    safe(seeker.getName()));
                y = drawLabelValue(cs, margin + 20, y, contentW - 40, "Email",        safe(seeker.getEmail()));
                if (seekerProfile != null) {
                    if (isNotBlank(seekerProfile.getLocation()))
                        y = drawLabelValue(cs, margin + 20, y, contentW - 40, "Location", seekerProfile.getLocation());
                    if (isNotBlank(seekerProfile.getExperience()))
                        y = drawLabelValue(cs, margin + 20, y, contentW - 40, "Experience", seekerProfile.getExperience());
                    if (isNotBlank(seekerProfile.getSkills()))
                        y = drawLabelValue(cs, margin + 20, y, contentW - 40, "Skills", seekerProfile.getSkills());
                    if (isNotBlank(seekerProfile.getLinkedinUrl()))
                        y = drawLabelValue(cs, margin + 20, y, contentW - 40, "LinkedIn", seekerProfile.getLinkedinUrl());
                }
                y = drawLabelValue(cs, margin + 20, y, contentW - 40, "Application #",
                        "JM-" + String.format("%06d", application.getId()));
                y = drawLabelValue(cs, margin + 20, y, contentW - 40, "Applied On",  safe(application.getAppliedAt()));

                // ── 11. Closing message ────────────────────────────────
                y -= 14;
                drawRect(cs, margin + 20, y, contentW - 40, 1, new float[]{0.88f, 0.88f, 0.92f});
                y -= 18;

                setFillColor(cs, DARK);
                if (accepted) {
                    String[] lines = {
                        "Our HR team will be in touch shortly to discuss the next steps and schedule",
                        "an interview. Please keep an eye on your inbox for further communication.",
                        "",
                        "We look forward to welcoming you to the team. Congratulations!"
                    };
                    for (String line : lines) {
                        cs.beginText();
                        cs.setFont(fontNormal(), 10);
                        cs.newLineAtOffset(margin + 20, y);
                        cs.showText(line);
                        cs.endText();
                        y -= 14;
                    }
                } else {
                    String[] lines = {
                        "We encouraged you to keep building your skills and exploring opportunities",
                        "on JobMitra. We wish you all the best in your future endeavours.",
                        "",
                        "You may re-apply for other relevant positions in the future."
                    };
                    for (String line : lines) {
                        cs.beginText();
                        cs.setFont(fontNormal(), 10);
                        cs.newLineAtOffset(margin + 20, y);
                        cs.showText(line);
                        cs.endText();
                        y -= 14;
                    }
                }

                // ── 12. Signature block ────────────────────────────────
                y -= 20;
                setFillColor(cs, DARK);
                cs.beginText();
                cs.setFont(fontNormal(), 10);
                cs.newLineAtOffset(margin + 20, y);
                cs.showText("Warm regards,");
                cs.endText();

                y -= 18;
                cs.beginText();
                cs.setFont(fontBold(), 11);
                cs.newLineAtOffset(margin + 20, y);
                cs.showText(safe(job.getRecruiterName() != null ? job.getRecruiterName() : "Recruitment Team"));
                cs.endText();

                y -= 14;
                setFillColor(cs, MED_GRAY);
                cs.beginText();
                cs.setFont(fontNormal(), 9);
                cs.newLineAtOffset(margin + 20, y);
                cs.showText(safe(job.getCompany()) + "  |  Powered by JobMitra");
                cs.endText();

                // ── 13. Footer band ────────────────────────────────────
                float footerH = 34f;
                drawRect(cs, 0, 0, pageW, footerH, BRAND_INDIGO);
                setFillColor(cs, WHITE);
                cs.beginText();
                cs.setFont(fontNormal(), 8);
                cs.newLineAtOffset(margin, 13);
                cs.showText("JobMitra  |  jobmitra-find-your-way.vercel.app  |  This is an auto-generated document — please do not reply to this email.");
                cs.endText();

                // Accent bottom line
                drawRect(cs, 0, footerH, pageW, 3, ACCENT_GOLD);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            return baos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate PDF letter: " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  DRAWING HELPERS
    // ─────────────────────────────────────────────────────────────────

    private void drawRect(PDPageContentStream cs, float x, float y, float w, float h,
                          float[] rgb) throws IOException {
        setFillColor(cs, rgb);
        cs.addRect(x, y, w, h);
        cs.fill();
    }

    /** Draws a filled rounded rectangle approximated with overlapping rects + circles at corners */
    private void drawRoundRect(PDPageContentStream cs, float x, float y, float w, float h,
                               float[] rgb) throws IOException {
        // Simple approximation: just draw a plain filled rect (looks fine for badges)
        drawRect(cs, x, y, w, h, rgb);
        // Slightly lightened overlay to simulate rounded feel
        float[] lighter = {
            Math.min(1f, rgb[0] + 0.06f),
            Math.min(1f, rgb[1] + 0.06f),
            Math.min(1f, rgb[2] + 0.06f)
        };
        drawRect(cs, x + 2, y, w - 4, h, lighter);
        drawRect(cs, x, y + 2, w, h - 4, lighter);
        // Core
        drawRect(cs, x + 2, y + 2, w - 4, h - 4, rgb);
    }

    private void setFillColor(PDPageContentStream cs, float[] rgb) throws IOException {
        cs.setNonStrokingColor(new PDColor(rgb, PDDeviceRGB.INSTANCE));
    }

    private void drawSectionHeader(PDPageContentStream cs, float x, float y,
                                   String text, float[] color) throws IOException {
        setFillColor(cs, color);
        cs.beginText();
        cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 8.5f);
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
    }

    /**
     * Draws a label-value row with alternating light background.
     * Returns the new Y position (shifted down by row height).
     */
    private float drawLabelValue(PDPageContentStream cs, float x, float y,
                                 float rowWidth, String label, String value) throws IOException {
        float rowH = 18f;

        // Light row background
        drawRect(cs, x, y - 4, rowWidth, rowH, new float[]{0.972f, 0.972f, 0.984f});

        // Label
        setFillColor(cs, MED_GRAY);
        cs.beginText();
        cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 8.5f);
        cs.newLineAtOffset(x + 6, y + 2);
        cs.showText(label + ":");
        cs.endText();

        // Value
        setFillColor(cs, DARK);
        cs.beginText();
        cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9f);
        cs.newLineAtOffset(x + 130, y + 2);
        // Truncate if too long
        String truncated = truncate(value, 55);
        cs.showText(truncated);
        cs.endText();

        return y - rowH;
    }

    // ─────────────────────────────────────────────────────────────────
    //  UTILITIES
    // ─────────────────────────────────────────────────────────────────

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "N/A" : s.trim();
    }

    private boolean isNotBlank(String s) {
        return s != null && !s.isBlank();
    }

    private String truncate(String s, int max) {
        if (s == null) return "N/A";
        s = s.trim();
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }
}
