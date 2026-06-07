package com.mgr.campusbridge.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * A report can target a USER (reportedUserId) or a piece of content
 * (targetType + targetId), e.g. a forum post or a resource.
 */
@Data
public class ReportRequest {

    // For user reports (optional now that content can be reported).
    private Long reportedUserId;

    // For content reports.
    private String targetType; // FORUM_POST | RESOURCE | USER
    private Long targetId;

    @NotBlank(message = "Reason is required")
    private String reason;

    private String description;
}
