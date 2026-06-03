package com.mgr.campusbridge.service;

import com.mgr.campusbridge.dto.request.ReportRequest;
import com.mgr.campusbridge.dto.response.ReportResponse;
import com.mgr.campusbridge.entity.Report;
import com.mgr.campusbridge.entity.User;
import com.mgr.campusbridge.exception.ResourceNotFoundException;
import com.mgr.campusbridge.repository.ReportRepository;
import com.mgr.campusbridge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReportResponse createReport(String reporterEmail, ReportRequest request) {
        User reporter = findByEmail(reporterEmail);
        User reportedUser = userRepository.findById(request.getReportedUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reported user not found: " + request.getReportedUserId()));
        Report report = Report.builder()
                .reporter(reporter)
                .reportedUser(reportedUser)
                .reason(request.getReason())
                .description(request.getDescription())
                .build();
        return ReportResponse.from(reportRepository.save(report));
    }

    private User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
}