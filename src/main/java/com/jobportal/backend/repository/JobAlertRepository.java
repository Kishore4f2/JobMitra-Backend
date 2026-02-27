package com.jobportal.backend.repository;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import com.jobportal.backend.model.JobAlert;

@Repository
public class JobAlertRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private RowMapper<JobAlert> rowMapper = (rs, rowNum) -> {
        JobAlert alert = new JobAlert();
        alert.setId(rs.getInt("id"));
        alert.setUserId(rs.getInt("user_id"));
        alert.setKeyword(rs.getString("keyword"));
        alert.setLocation(rs.getString("location"));
        alert.setCreatedAt(
                rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        return alert;
    };

    public int save(JobAlert alert) {
        String sql = "INSERT INTO job_alerts(user_id, keyword, location) VALUES(?, ?, ?)";
        return jdbcTemplate.update(sql, alert.getUserId(), alert.getKeyword(), alert.getLocation());
    }

    public List<JobAlert> findByUserId(int userId) {
        String sql = "SELECT * FROM job_alerts WHERE user_id = ?";
        return jdbcTemplate.query(sql, rowMapper, userId);
    }

    public int deleteById(int id, int userId) {
        String sql = "DELETE FROM job_alerts WHERE id = ? AND user_id = ?";
        return jdbcTemplate.update(sql, id, userId);
    }

    public List<JobAlert> findAll() {
        String sql = "SELECT * FROM job_alerts";
        return jdbcTemplate.query(sql, rowMapper);
    }
}
