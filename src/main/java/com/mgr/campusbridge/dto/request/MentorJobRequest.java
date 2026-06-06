package com.mgr.campusbridge.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class MentorJobRequest {
    private String title;
    private String company;
    private String location;
    private String jobType;
    private String description;
    private String applyLink;
    private List<String> skills;
}
