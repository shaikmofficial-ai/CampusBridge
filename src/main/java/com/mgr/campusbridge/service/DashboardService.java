package com.mgr.campusbridge.service;

import com.mgr.campusbridge.dto.response.DashboardResponse;
import com.mgr.campusbridge.entity.*;
import com.mgr.campusbridge.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final MentorConnectionRepository connectionRepository;
    private final ForumPostRepository forumPostRepository;

    public DashboardResponse getDashboard(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        long mentorsConnected = connectionRepository
                .countByStudentAndStatus(user, MentorConnection.Status.ACCEPTED);
        long forumInteractions = forumPostRepository.countByAuthorId(user.getId());

        DashboardResponse response = new DashboardResponse();
        response.setUserName(user.getName());
        response.setMentorsConnected(mentorsConnected);
        response.setResourcesSaved(0);
        response.setForumInteractions(forumInteractions);
        response.setCommunityPoints(user.getCommunityPoints());
        return response;
    }
}