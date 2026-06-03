package com.mgr.campusbridge.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class DashboardResponse {
    private Long userId;
    private String userName;
    private String role;
    private long mentorsConnected;
    private long resourcesSaved;
    private long forumInteractions;
    private int communityPoints;
    private List<PlacementDriveResponse> upcomingPlacementDrives;
    private List<ProfileResponse> recommendedMentors;
}