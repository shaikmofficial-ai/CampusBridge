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
    private final ForumCommentRepository forumCommentRepository;
    private final ResourceRepository resourceRepository;
    private final SavedResourceRepository savedResourceRepository;
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

    /** Permanently delete a reported/inappropriate forum post (admin only). */
    @Transactional
    public void deleteForumPost(Long postId) {
        var post = forumPostRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Forum post not found: " + postId));
        // Remove comments first to satisfy FK constraints.
        forumCommentRepository.deleteAll(forumCommentRepository.findByPostIdOrderByCreatedAtAsc(postId));
        forumPostRepository.delete(post);
        autoResolveContentReports(Report.TargetType.FORUM_POST, postId);
    }

    /** Permanently delete a reported/inappropriate resource (admin only). */
    @Transactional
    public void deleteResource(Long resourceId) {
        var resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found: " + resourceId));
        // Remove saved-bookmarks referencing this resource first.
        savedResourceRepository.deleteAll(savedResourceRepository.findByResource(resource));
        // Best-effort: delete the stored file.
        try {
            if (resource.getFilePath() != null) {
                java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(resource.getFilePath()));
            }
        } catch (Exception ignored) { /* non-fatal */ }
        resourceRepository.delete(resource);
        autoResolveContentReports(Report.TargetType.RESOURCE, resourceId);
    }

    /** Mark any open reports for a now-deleted item as resolved. */
    private void autoResolveContentReports(Report.TargetType type, Long targetId) {
        reportRepository.findByTargetTypeAndTargetId(type, targetId).forEach(r -> {
            if (r.getStatus() == Report.ReportStatus.OPEN) {
                r.setStatus(Report.ReportStatus.RESOLVED);
                reportRepository.save(r);
            }
        });
    }

    private User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }
}