package com.mgr.campusbridge.dto.response;

import com.mgr.campusbridge.entity.PlacementDrive;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class PlacementDriveResponse {
    private Long id;
    private String companyName;
    private String role;
    private String packageAmount;
    private String location;
    private String eligibilityCriteria;
    private LocalDate applicationDeadline;
    private String applicationLink;
    private String description;
    private String status;
    private LocalDateTime createdAt;

    public static PlacementDriveResponse from(PlacementDrive drive) {
        return PlacementDriveResponse.builder()
                .id(drive.getId())
                .companyName(drive.getCompanyName())
                .role(drive.getRole())
                .packageAmount(drive.getPackageAmount())
                .location(drive.getLocation())
                .eligibilityCriteria(drive.getEligibilityCriteria())
                .applicationDeadline(drive.getApplicationDeadline())
                .applicationLink(drive.getApplicationLink())
                .description(drive.getDescription())
                .status(drive.getStatus().name())
                .createdAt(drive.getCreatedAt())
                .build();
    }
}