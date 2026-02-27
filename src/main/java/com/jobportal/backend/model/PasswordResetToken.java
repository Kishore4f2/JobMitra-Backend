package com.jobportal.backend.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken {
    private int id;
    private String token;
    private int userId;
    private LocalDateTime expiryDate;
}
