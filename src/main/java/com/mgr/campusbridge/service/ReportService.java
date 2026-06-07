package com.mgr.campusbridge.service;

import com.mgr.campusbridge.dto.request.ReportRequest;
import com.mgr.campusbridge.dto.response.ReportResponse;
import com.mgr.campusbridge.entity.Report;
import com.mgr.campusbridge.entity.User;
import com.mgr.campusbridge.exception.ResourceNotFoundException;
import com.mgr.campusbridge.exception.UnauthorizedException;
import com.mgr.campusbridge.repository.ForumPostRepository;
import com.mgr.campusbridge.repository.ReportRepository;
import com.mgr.campusbridge.repository.ResourceRepository;
import com.mgr.campusbridge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final ForumPostRepository forumPostRepository;
    private final ResourceRepository resourceRepository;

    @Transactional
    public ReportResponse createReport(String reporterEmail, ReportRequest request) {
        User reporter = findByEmail(reporterEmail);

        Report.TargetType targetType = Report.TargetType.USER;
        if (StringUtils.hasText(request.getTargetType())) {
            try {
                targetType = Report.TargetType.valueOf(request.getTargetType().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new UnauthorizedException("Invalid report target type: " + request.getTargetType());
            }
        }

        Report.ReportBuilder builder = Report.builder()
                .reporter(reporter)
                .reason(request.getReason())
                .description(request.getDescription())
                .targetType(targetType);

        switch (targetType) {
            case USER -> {
                if (request.getReportedUserId() == null) {
                    throw new UnauthorizedException("reportedUserId is required for user reports.");
                }
                User reportedUser = userRepository.findById(request.getReportedUserId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Reported user not found: " + request.getReportedUserId()));
                builder.reportedUser(reportedUser)
                        .targetId(reportedUser.getId())
                        .targetTitle(reportedUser.getName());
            }
            case FORUM_POST -> {
                if (request.getTargetId() == null) {
                    throw new UnauthorizedException("targetId is required for content reports.");
                }
                var post = forumPostRepository.findById(request.getTargetId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Forum post not found: " + request.getTargetId()));
                builder.targetId(post.getId()).targetTitle(post.getTitle());
            }
            case RESOURCE -> {
                if (request.getTargetId() == null) {
                    throw new UnauthorizedException("targetId is required for content reports.");
                }
                var resource = resourceRepository.findById(request.getTargetId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Resource not found: " + request.getTargetId()));
                builder.targetId(resource.getId()).targetTitle(resource.getTitle());
            }
        }

        return ReportResponse.from(reportRepository.save(builder.build()));
    }

    private User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
}
