package com.mgr.campusbridge.dto.response;

import com.mgr.campusbridge.entity.PlacementStory;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class PlacementStoryResponse {
    private Long id;
    private String companyName;
    private String studentName;
    private String role;
    private String packageAmount;
    private String story;
    private String imageUrl;
    private LocalDateTime createdAt;

    public static PlacementStoryResponse from(PlacementStory s) {
        return PlacementStoryResponse.builder()
                .id(s.getId())
                .companyName(s.getCompanyName())
                .studentName(s.getStudentName())
                .role(s.getRole())
                .packageAmount(s.getPackageAmount())
                .story(s.getStory())
                .imageUrl(s.getImageUrl())
                .createdAt(s.getCreatedAt())
                .build();
    }
}