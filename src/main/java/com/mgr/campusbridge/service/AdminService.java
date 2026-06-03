package com.mgr.campusbridge.service;

import com.mgr.campusbridge.dto.response.AdminStatsResponse;
import com.mgr.campusbridge.dto.response.ProfileResponse;
import com.mgr.campusbridge.dto.response.ReportResponse;
import com.mgr.campusbridge.entity.Report;
import com.mgr.campusbridge.entity.User;
import com.mgr.campusbridge.exception.ResourceNotFoundException;
import com.mgr.campusbridge.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ForumPostRepository forumPostRepository;
    private final ResourceRepository resourceRepository;
    private final PlacementDriveRepository placementDriveRepository;
    private final ReportRepository reportRepository;

    public AdminStatsResponse getStats() {
        return AdminStatsResponse.builder()
                .totalUsers(userRepository.count())
                .totalStudents(userRepository.countByRole(User.Role.STUDENT))
                .totalMentors(userRepository.countByRole(User.Role.MENTOR))
                .totalForumPosts(forumPostRepository.count())
                .totalResources(resourceRepository.count())
                .totalPlacementDrives(placementDriveRepository.count())
                .pendingVerifications(userRepository.countByAccountStatus(User.AccountStatus.PENDING))
                .openReports(reportRepository.countByStatus(Report.ReportStatus.OPEN))
                .build();
    }

    public List<ProfileResponse> getPendingUsers() {
        return userRepository.findByAccountStatus(User.AccountStatus.PENDING)
                .stream().map(ProfileResponse::from).collect(Collectors.toList());
    }

    @Transactional
    public ProfileResponse approveUser(Long userId) {
        User user = findById(userId);
        user.setAccountStatus(User.AccountStatus.APPROVED);
        return ProfileResponse.from(userRepository.save(user));
    }

    @Transactional
    public ProfileResponse rejectUser(Long userId) {
        User user = findById(userId);
        user.setAccountStatus(User.AccountStatus.REJECTED);
        return ProfileResponse.from(userRepository.save(user));
    }

    public List<ReportResponse> getOpenReports() {
        return reportRepository.findByStatusOrderByCreatedAtDesc(Report.ReportStatus.OPEN)
                .stream().map(ReportResponse::from).collect(Collectors.toList());
    }

    @Transactional
    public ReportResponse resolveReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found: " + reportId));
        report.setStatus(Report.ReportStatus.RESOLVED);
        return ReportResponse.from(reportRepository.save(report));
    }

    private User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }
}