package com.mgr.campusbridge.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminStatsResponse {
    private long totalUsers;
    private long totalStudents;
    private long totalMentors;
    private long totalForumPosts;
    private long totalResources;
    private long totalPlacementDrives;
    private long pendingVerifications;
    private long openReports;
}