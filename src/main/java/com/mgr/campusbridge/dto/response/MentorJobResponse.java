package com.mgr.campusbridge.dto.response;

import com.mgr.campusbridge.entity.MentorJob;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class MentorJobResponse {
    private Long id;
    private String title;
    private String company;
    private String location;
    private String jobType;
    private String description;
    private String applyLink;
    private List<String> skills;
    private Long mentorId;
    private String mentorName;
    private String mentorDesignation;
    private LocalDateTime createdAt;

    public static MentorJobResponse from(MentorJob job) {
        return MentorJobResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .company(job.getCompany())
                .location(job.getLocation())
                .jobType(job.getJobType())
                .description(job.getDescription())
                .applyLink(job.getApplyLink())
                .skills(job.getSkills())
                .mentorId(job.getPostedBy() != null ? job.getPostedBy().getId() : null)
                .mentorName(job.getPostedBy() != null ? job.getPostedBy().getName() : "Mentor")
                .mentorDesignation(job.getPostedBy() != null ? job.getPostedBy().getDepartment() : null)
                .createdAt(job.getCreatedAt())
                .build();
    }
}
