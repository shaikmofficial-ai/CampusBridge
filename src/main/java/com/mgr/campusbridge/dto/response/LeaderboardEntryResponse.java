package com.mgr.campusbridge.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class LeaderboardEntryResponse {
    private int rank;
    private Long userId;
    private String name;
    private String department;
    private String batch;
    private String profilePictureUrl;
    private long lessonsSolved;
    private int totalPoints;
}
