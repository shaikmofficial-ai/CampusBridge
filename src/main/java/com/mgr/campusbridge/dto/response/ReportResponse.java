package com.mgr.campusbridge.dto.response;

import com.mgr.campusbridge.entity.Report;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ReportResponse {
    private Long id;
    private Long reporterId;
    private String reporterName;
    private Long reportedUserId;
    private String reportedUserName;
    private String targetType;
    private Long targetId;
    private String targetTitle;
    private String reason;
    private String description;
    private String status;
    private LocalDateTime createdAt;

    public static ReportResponse from(Report r) {
        return ReportResponse.builder()
                .id(r.getId())
                .reporterId(r.getReporter() != null ? r.getReporter().getId() : null)
                .reporterName(r.getReporter() != null ? r.getReporter().getName() : null)
                .reportedUserId(r.getReportedUser() != null ? r.getReportedUser().getId() : null)
                .reportedUserName(r.getReportedUser() != null ? r.getReportedUser().getName() : null)
                .targetType(r.getTargetType() != null ? r.getTargetType().name() : "USER")
                .targetId(r.getTargetId())
                .targetTitle(r.getTargetTitle())
                .reason(r.getReason())
                .description(r.getDescription())
                .status(r.getStatus().name())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
