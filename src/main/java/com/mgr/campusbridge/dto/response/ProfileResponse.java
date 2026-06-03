package com.mgr.campusbridge.dto.response;

import com.mgr.campusbridge.entity.User;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ProfileResponse {
    private Long id;
    private String name;
    private String email;
    private String role;
    private String department;
    private String batch;
    private String bio;
    private String linkedinUrl;
    private String githubUrl;
    private String portfolioUrl;
    private String profilePictureUrl;
    private List<String> skills;
    private List<String> achievements;
    private int communityPoints;
    private String accountStatus;

    public static ProfileResponse from(User user) {
        return ProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .department(user.getDepartment())
                .batch(user.getBatch())
                .bio(user.getBio())
                .linkedinUrl(user.getLinkedinUrl())
                .githubUrl(user.getGithubUrl())
                .portfolioUrl(user.getPortfolioUrl())
                .profilePictureUrl(user.getProfilePictureUrl())
                .skills(user.getSkills())
                .achievements(user.getAchievements())
                .communityPoints(user.getCommunityPoints())
                .accountStatus(
                        user.getAccountStatus() != null
                                ? user.getAccountStatus().name()
                                : "PENDING"
                )
                .build();
    }
}