package com.jobportal.backend.repository;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import com.jobportal.backend.model.SavedJob;

@Repository
public class SavedJobRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private RowMapper<SavedJob> rowMapper = (rs, rowNum) -> {
        SavedJob sj = new SavedJob();
        sj.setId(rs.getInt("id"));
        sj.setSeekerId(rs.getInt("seeker_id"));
        sj.setJobId(rs.getInt("job_id"));
        sj.setSavedAt(rs.getString("saved_at"));
        try {
            sj.setTitle(rs.getString("title"));
        } catch (Exception ignored) {
        }
        try {
            sj.setCompany(rs.getString("company"));
        } catch (Exception ignored) {
        }
        try {
            sj.setLocation(rs.getString("location"));
        } catch (Exception ignored) {
        }
        try {
            sj.setJobType(rs.getString("job_type"));
        } catch (Exception ignored) {
        }
        try {
            sj.setSalaryRange(rs.getString("salary_range"));
        } catch (Exception ignored) {
        }
        try {
            sj.setDeadline(rs.getString("deadline"));
        } catch (Exception ignored) {
        }
        return sj;
    };

    public int save(int seekerId, int jobId) {
        String sql = "INSERT INTO saved_jobs(seeker_id, job_id) VALUES(?, ?) ON DUPLICATE KEY UPDATE seeker_id=seeker_id";
        return jdbcTemplate.update(sql, seekerId, jobId);
    }

    public int delete(int seekerId, int jobId) {
        String sql = "DELETE FROM saved_jobs WHERE seeker_id=? AND job_id=?";
        return jdbcTemplate.update(sql, seekerId, jobId);
    }

    public List<SavedJob> findBySeekerId(int seekerId) {
        String sql = "SELECT sj.id, sj.seeker_id, sj.job_id, sj.saved_at, " +
                "j.title, j.company, j.location, j.job_type, j.salary_range, j.deadline " +
                "FROM saved_jobs sj JOIN jobs j ON sj.job_id = j.id " +
                "WHERE sj.seeker_id = ? ORDER BY sj.saved_at DESC";
        return jdbcTemplate.query(sql, rowMapper, seekerId);
    }

    public boolean exists(int seekerId, int jobId) {
        String sql = "SELECT COUNT(*) FROM saved_jobs WHERE seeker_id=? AND job_id=?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, seekerId, jobId);
        return count != null && count > 0;
    }
}
