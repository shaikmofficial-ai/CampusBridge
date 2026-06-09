package com.mgr.campusbridge.dto.response;

import com.mgr.campusbridge.entity.User;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Public-facing portfolio view. Deliberately omits private/administrative
 * fields: password (never serialized anyway), account approval status, ban
 * state, and email is included only as a contact field for the portfolio.
 */
@Data
@Builder
public class PublicProfileResponse {
    private Long id;
    private String name;
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

    public static PublicProfileResponse from(User user) {
        return PublicProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
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
                .build();
    }
}
