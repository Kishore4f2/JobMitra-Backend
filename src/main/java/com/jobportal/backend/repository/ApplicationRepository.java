package com.jobportal.backend.repository;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import com.jobportal.backend.model.Application;

@Repository
public class ApplicationRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private RowMapper<Application> rowMapper = (rs, rowNum) -> {
        Application app = new Application();
        app.setId(rs.getInt("id"));
        app.setJobId(rs.getInt("job_id"));
        app.setSeekerId(rs.getInt("seeker_id"));
        app.setSeekerName(rs.getString("seeker_name"));
        app.setSeekerEmail(rs.getString("seeker_email"));
        app.setResumeFile(rs.getString("resume_file"));
        app.setStatus(rs.getString("status"));
        app.setAppliedAt(rs.getString("applied_at"));

        // Try to get job_title if it exists in the result set (from JOIN)
        try {
            app.setJobTitle(rs.getString("job_title"));
        } catch (Exception e) {
            // Not in result set, skip
        }

        return app;
    };

    public int save(Application app) {
        String sql = "INSERT INTO applications(job_id, seeker_id, seeker_name, seeker_email, resume_file, status) VALUES(?,?,?,?,?,?)";
        return jdbcTemplate.update(sql, app.getJobId(), app.getSeekerId(), app.getSeekerName(), app.getSeekerEmail(),
                app.getResumeFile(), app.getStatus());
    }

    public List<Application> findBySeekerId(int seekerId) {
        String sql = "SELECT a.*, j.title as job_title FROM applications a LEFT JOIN jobs j ON a.job_id = j.id WHERE a.seeker_id = ?";
        return jdbcTemplate.query(sql, rowMapper, seekerId);
    }

    public List<Application> findByJobId(int jobId) {
        String sql = "SELECT a.*, j.title as job_title FROM applications a LEFT JOIN jobs j ON a.job_id = j.id WHERE a.job_id = ?";
        return jdbcTemplate.query(sql, rowMapper, jobId);
    }

    public int updateStatus(int id, String status) {
        String sql = "UPDATE applications SET status = ? WHERE id = ?";
        return jdbcTemplate.update(sql, status, id);
    }

    public Application findById(int id) {
        String sql = "SELECT a.*, j.title as job_title FROM applications a LEFT JOIN jobs j ON a.job_id = j.id WHERE a.id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, rowMapper, id);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<Application> findAll() {
        String sql = "SELECT a.*, j.title as job_title FROM applications a LEFT JOIN jobs j ON a.job_id = j.id ORDER BY a.applied_at DESC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public int deleteById(int id) {
        String sql = "DELETE FROM applications WHERE id=?";
        return jdbcTemplate.update(sql, id);
    }
}
