package com.jobportal.backend.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.jobportal.backend.model.Job;

@Repository
public class JobRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // ⭐ RowMapper
    private RowMapper<Job> rowMapper = (rs, rowNum) -> {
        Job job = new Job();
        job.setId(rs.getInt("id"));
        job.setTitle(rs.getString("title"));
        job.setCompany(rs.getString("company"));
        job.setLocation(rs.getString("location"));
        job.setDescription(rs.getString("description"));
        job.setRecruiterId(rs.getInt("recruiter_id"));
        job.setJobType(rs.getString("job_type"));
        job.setSalaryRange(rs.getString("salary_range"));
        job.setLogo(rs.getString("logo"));
        job.setPostedAt(rs.getString("posted_at"));
        job.setExperience(rs.getString("experience"));
        job.setDeadline(rs.getString("deadline"));

        String skillsStr = rs.getString("skills");
        if (skillsStr != null && !skillsStr.isEmpty()) {
            job.setSkills(java.util.Arrays.asList(skillsStr.split(",")));
        }

        try {
            job.setRecruiterName(rs.getString("recruiter_name"));
        } catch (Exception e) {
            // Not in result set
        }

        return job;
    };

    // ⭐ CREATE JOB
    public int save(Job job) {
        String sql = "INSERT INTO jobs(title,company,location,description,recruiter_id,job_type,salary_range,skills,logo,posted_at,experience,deadline) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";

        System.out.println("JobRepository: Saving job with title: " + job.getTitle());
        System.out.println("JobRepository: Recruiter ID: " + job.getRecruiterId());

        String skillsStr = "";
        if (job.getSkills() != null) {
            skillsStr = String.join(",", job.getSkills());
        }

        return jdbcTemplate.update(sql,
                job.getTitle(),
                job.getCompany(),
                job.getLocation(),
                job.getDescription(),
                job.getRecruiterId(),
                job.getJobType(),
                job.getSalaryRange(),
                skillsStr,
                job.getLogo(),
                job.getPostedAt(),
                job.getExperience(),
                job.getDeadline());
    }

    // ⭐ GET ALL JOBS
    public List<Job> findAll() {
        String sql = "SELECT j.*, u.name as recruiter_name FROM jobs j LEFT JOIN users u ON j.recruiter_id = u.id ORDER BY j.id DESC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    // ⭐ GET PAGINATED JOBS
    public List<Job> findPaginated(int page, int size) {
        int offset = page * size;
        String sql = "SELECT j.*, u.name as recruiter_name FROM jobs j LEFT JOIN users u ON j.recruiter_id = u.id ORDER BY j.id DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, rowMapper, size, offset);
    }

    // ⭐ SEARCH BY TITLE
    public List<Job> searchByTitle(String title) {
        String sql = "SELECT * FROM jobs WHERE title LIKE ?";
        return jdbcTemplate.query(sql, rowMapper, "%" + title + "%");
    }

    // ⭐ FILTER BY LOCATION
    public List<Job> findByLocation(String location) {
        String sql = "SELECT * FROM jobs WHERE location=?";
        return jdbcTemplate.query(sql, rowMapper, location);
    }

    // ⭐ JOBS BY RECRUITER
    public List<Job> findByRecruiter(int recruiterId) {
        String sql = "SELECT * FROM jobs WHERE recruiter_id=?";
        return jdbcTemplate.query(sql, rowMapper, recruiterId);
    }

    public Job findById(int id) {
        String sql = "SELECT * FROM jobs WHERE id=?";
        try {
            return jdbcTemplate.queryForObject(sql, rowMapper, id);
        } catch (Exception e) {
            return null;
        }
    }

    public java.util.Map<String, Long> getRecruiterStats(int recruiterId) {
        String jobsSql = "SELECT COUNT(*) FROM jobs WHERE recruiter_id=?";
        String appsSql = "SELECT COUNT(*) FROM applications a JOIN jobs j ON a.job_id = j.id WHERE j.recruiter_id=?";
        String statusSql = "SELECT a.status, COUNT(*) as count FROM applications a JOIN jobs j ON a.job_id = j.id WHERE j.recruiter_id=? GROUP BY a.status";

        long totalJobs = jdbcTemplate.queryForObject(jobsSql, Long.class, recruiterId);
        long totalApplicants = jdbcTemplate.queryForObject(appsSql, Long.class, recruiterId);

        java.util.Map<String, Long> stats = new java.util.HashMap<>();
        stats.put("totalJobs", totalJobs);
        stats.put("totalApplicants", totalApplicants);
        stats.put("shortlisted", 0L);
        stats.put("rejected", 0L);
        stats.put("pending", 0L);

        jdbcTemplate.query(statusSql, (rs) -> {
            String status = rs.getString("status");
            long count = rs.getLong("count");
            if ("hr_round".equals(status) || "shortlisted".equals(status)) {
                stats.put("shortlisted", stats.get("shortlisted") + count);
            } else if ("rejected".equals(status)) {
                stats.put("rejected", count);
            } else if ("pending".equals(status)) {
                stats.put("pending", count);
            }
        }, recruiterId);

        return stats;
    }

    public int deleteById(int id) {
        String sql = "DELETE FROM jobs WHERE id=?";
        return jdbcTemplate.update(sql, id);
    }
}
