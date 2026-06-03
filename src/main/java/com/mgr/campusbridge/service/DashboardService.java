package com.mgr.campusbridge.service;

import com.mgr.campusbridge.dto.response.DashboardResponse;
import com.mgr.campusbridge.dto.response.PlacementDriveResponse;
import com.mgr.campusbridge.dto.response.ProfileResponse;
import com.mgr.campusbridge.entity.MentorConnection;
import com.mgr.campusbridge.entity.PlacementDrive;
import com.mgr.campusbridge.entity.User;
import com.mgr.campusbridge.exception.ResourceNotFoundException;
import com.mgr.campusbridge.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final MentorConnectionRepository connectionRepository;
    private final SavedResourceRepository savedResourceRepository;
    private final ForumPostRepository forumPostRepository;
    private final PlacementDriveRepository placementDriveRepository;

    public DashboardResponse getDashboard(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        long mentorsConnected = connectionRepository.countByStudentAndStatus(
                user, MentorConnection.Status.ACCEPTED);
        long resourcesSaved = savedResourceRepository.countByUser(user);
        long forumInteractions = forumPostRepository.countByAuthorId(user.getId());

        List<PlacementDriveResponse> upcomingDrives = placementDriveRepository
                .findByStatusOrderByApplicationDeadlineAsc(PlacementDrive.DriveStatus.OPEN)
                .stream().limit(3)
                .map(PlacementDriveResponse::from)
                .collect(Collectors.toList());

        List<ProfileResponse> recommendedMentors = userRepository
                .findByRoleAndAccountStatus(User.Role.MENTOR, User.AccountStatus.APPROVED)
                .stream().limit(4)
                .map(ProfileResponse::from)
                .collect(Collectors.toList());

        return DashboardResponse.builder()
                .userId(user.getId())
                .userName(user.getName())
                .role(user.getRole().name())
                .mentorsConnected(mentorsConnected)
                .resourcesSaved(resourcesSaved)
                .forumInteractions(forumInteractions)
                .communityPoints(user.getCommunityPoints())
                .upcomingPlacementDrives(upcomingDrives)
                .recommendedMentors(recommendedMentors)
                .build();
    }
}