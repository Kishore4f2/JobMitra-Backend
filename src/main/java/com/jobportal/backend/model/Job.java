package com.jobportal.backend.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Job {

    private int id;
    private String title;
    private String company;
    private String location;
    private String description;
    @JsonProperty("recruiterId")
    private int recruiterId;
    private String jobType;
    private String salaryRange;
    private String experience;
    private java.util.List<String> skills;
    private String logo;
    private String postedAt;
    private String recruiterName;
    private String deadline;

}
