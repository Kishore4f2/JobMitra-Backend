package com.jobportal.backend.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import com.jobportal.backend.model.PasswordResetToken;
import java.sql.Timestamp;

@Repository
public class PasswordResetTokenRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private RowMapper<PasswordResetToken> rowMapper = (rs, rowNum) -> {
        PasswordResetToken token = new PasswordResetToken();
        token.setId(rs.getInt("id"));
        token.setToken(rs.getString("token"));
        token.setUserId(rs.getInt("user_id"));
        token.setExpiryDate(rs.getTimestamp("expiry_date").toLocalDateTime());
        return token;
    };

    public int save(PasswordResetToken token) {
        String sql = "INSERT INTO password_reset_tokens(token, user_id, expiry_date) VALUES(?, ?, ?)";
        return jdbcTemplate.update(sql, token.getToken(), token.getUserId(), Timestamp.valueOf(token.getExpiryDate()));
    }

    public PasswordResetToken findByToken(String token) {
        String sql = "SELECT * FROM password_reset_tokens WHERE token = ?";
        try {
            return jdbcTemplate.queryForObject(sql, rowMapper, token);
        } catch (Exception e) {
            return null;
        }
    }

    public void deleteByUserId(int userId) {
        String sql = "DELETE FROM password_reset_tokens WHERE user_id = ?";
        jdbcTemplate.update(sql, userId);
    }
}
