package com.mgr.campusbridge.dto.response;

import com.mgr.campusbridge.entity.MentorPlacement;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MentorPlacementResponse {
    private Long id;
    private String studentName;
    private Long studentId;
    private String batch;
    private String company;
    private String role;
    private String packageAmount;
    private String studentProfilePictureUrl;
    private LocalDateTime createdAt;

    public static MentorPlacementResponse from(MentorPlacement p) {
        return MentorPlacementResponse.builder()
                .id(p.getId())
                .studentName(p.getStudentName())
                .studentId(p.getStudent() != null ? p.getStudent().getId() : null)
                .batch(p.getBatch())
                .company(p.getCompany())
                .role(p.getRole())
                .packageAmount(p.getPackageAmount())
                .studentProfilePictureUrl(p.getStudent() != null ? p.getStudent().getProfilePictureUrl() : null)
                .createdAt(p.getCreatedAt())
                .build();
    }
}
