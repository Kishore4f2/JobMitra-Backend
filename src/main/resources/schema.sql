-- Disable foreign key checks to allow dropping tables in any order
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `password_reset_tokens`;
DROP TABLE IF EXISTS `job_alerts`;
DROP TABLE IF EXISTS `saved_jobs`;
DROP TABLE IF EXISTS `user_profiles`;
DROP TABLE IF EXISTS `applications`;
DROP TABLE IF EXISTS `jobs`;
DROP TABLE IF EXISTS `users`;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE `users` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(255) NOT NULL,
    `email` VARCHAR(255) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `role` VARCHAR(50) NOT NULL,
    `status` VARCHAR(20) DEFAULT 'active',
    `resume_file` VARCHAR(255),
    `resume_link` VARCHAR(255)
);

-- Seed Admin User
INSERT INTO `users` (`name`, `email`, `password`, `role`, `status`) 
VALUES ('Satya Kishore', 'satyakishore273@gmail.com', 'kissu123', 'ADMIN', 'active');

CREATE TABLE `jobs` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(255) NOT NULL,
    `company` VARCHAR(255) NOT NULL,
    `location` VARCHAR(255),
    `description` TEXT,
    `recruiter_id` INT,
    `job_type` VARCHAR(50),
    `salary_range` VARCHAR(100),
    `skills` TEXT,
    `logo` VARCHAR(255),
    `posted_at` VARCHAR(50),
    `experience` VARCHAR(100),
    `deadline` VARCHAR(100),
    FOREIGN KEY (`recruiter_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
);

CREATE TABLE `applications` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `job_id` INT,
    `seeker_id` INT,
    `seeker_name` VARCHAR(255),
    `seeker_email` VARCHAR(255),
    `resume_file` VARCHAR(255),
    `status` VARCHAR(50) DEFAULT 'pending',
    `applied_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`job_id`) REFERENCES `jobs`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`seeker_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
);

CREATE TABLE `user_profiles` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT NOT NULL,
    `skills` TEXT,
    `bio` TEXT,
    `experience` VARCHAR(100),
    `location` VARCHAR(255),
    `linkedin_url` VARCHAR(255),
    `photo_url` VARCHAR(500),
    UNIQUE KEY `uq_user_profiles_user_id` (`user_id`),
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
);

CREATE TABLE `saved_jobs` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `seeker_id` INT NOT NULL,
    `job_id` INT NOT NULL,
    `saved_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uq_saved_jobs` (`seeker_id`, `job_id`),
    FOREIGN KEY (`seeker_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`job_id`) REFERENCES `jobs`(`id`) ON DELETE CASCADE
);

CREATE TABLE `job_alerts` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT NOT NULL,
    `keyword` VARCHAR(255),
    `location` VARCHAR(255),
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
);

CREATE TABLE `password_reset_tokens` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `token` VARCHAR(255) NOT NULL,
    `user_id` INT NOT NULL,
    `expiry_date` TIMESTAMP NOT NULL,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
);
