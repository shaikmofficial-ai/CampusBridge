package com.mgr.campusbridge.dto.request;

import lombok.Data;

@Data
public class MentorPlacementRequest {
    private String studentName;
    private Long studentId;     // optional link to a registered student
    private String batch;
    private String company;
    private String role;
    private String packageAmount;
}
