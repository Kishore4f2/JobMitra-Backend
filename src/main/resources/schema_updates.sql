-- SQL Migration Script for JobMitra Enhancements

-- 1. Create user_profiles table for extended seeker data
CREATE TABLE IF NOT EXISTS user_profiles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    skills TEXT,
    bio TEXT,
    experience VARCHAR(100),
    location VARCHAR(255),
    linkedin_url VARCHAR(255),
    photo_url VARCHAR(255),
    UNIQUE KEY(user_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 2. Create saved_jobs table for bookmarking
CREATE TABLE IF NOT EXISTS saved_jobs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    seeker_id INT NOT NULL,
    job_id INT NOT NULL,
    saved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY(seeker_id, job_id),
    FOREIGN KEY (seeker_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE
);

-- 3. Create job_alerts table for notifications
CREATE TABLE IF NOT EXISTS job_alerts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    keyword VARCHAR(255),
    location VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 4. Create password_reset_tokens table
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id INT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(255) NOT NULL,
    user_id INT NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Note: Ensure your 'jobs' table has 'deadline' and 'experience' columns.
-- If not, run these:
-- ALTER TABLE jobs ADD COLUMN IF NOT EXISTS deadline VARCHAR(50);
-- ALTER TABLE jobs ADD COLUMN IF NOT EXISTS experience VARCHAR(50);
-- ALTER TABLE jobs ADD COLUMN IF NOT EXISTS skills TEXT;
