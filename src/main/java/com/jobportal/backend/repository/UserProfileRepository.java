package com.jobportal.backend.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.jobportal.backend.model.UserProfile;

@Repository
public class UserProfileRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private org.springframework.jdbc.core.RowMapper<UserProfile> rowMapper = (rs, rowNum) -> {
        UserProfile p = new UserProfile();
        p.setId(rs.getInt("id"));
        p.setUserId(rs.getInt("user_id"));
        p.setSkills(rs.getString("skills"));
        p.setBio(rs.getString("bio"));
        p.setExperience(rs.getString("experience"));
        p.setLocation(rs.getString("location"));
        p.setLinkedinUrl(rs.getString("linkedin_url"));
        try {
            p.setPhotoUrl(rs.getString("photo_url"));
        } catch (Exception ignored) {
        }
        return p;
    };

    public UserProfile findByUserId(int userId) {
        String sql = "SELECT * FROM user_profiles WHERE user_id=?";
        try {
            return jdbcTemplate.queryForObject(sql, rowMapper, userId);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null;
        }
    }

    public int save(UserProfile profile) {
        String sql = "INSERT INTO user_profiles(user_id, skills, bio, experience, location, linkedin_url) " +
                "VALUES(?,?,?,?,?,?) ON DUPLICATE KEY UPDATE " +
                "skills=VALUES(skills), bio=VALUES(bio), experience=VALUES(experience), " +
                "location=VALUES(location), linkedin_url=VALUES(linkedin_url)";
        return jdbcTemplate.update(sql,
                profile.getUserId(),
                profile.getSkills(),
                profile.getBio(),
                profile.getExperience(),
                profile.getLocation(),
                profile.getLinkedinUrl());
    }

    public int updatePhotoUrl(int userId, String photoUrl) {
        // Use a single upsert - insert or update photo_url in one atomic statement
        String sql = "INSERT INTO user_profiles(user_id, photo_url, skills, bio, experience, location, linkedin_url) " +
                "VALUES(?, ?, '', '', '', '', '') " +
                "ON DUPLICATE KEY UPDATE photo_url=VALUES(photo_url)";
        return jdbcTemplate.update(sql, userId, photoUrl);
    }
}
