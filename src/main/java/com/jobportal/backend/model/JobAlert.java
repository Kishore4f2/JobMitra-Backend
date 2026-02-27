package com.jobportal.backend.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobAlert {
    private int id;
    private int userId;
    private String keyword;
    private String location;
    private LocalDateTime createdAt;
}
