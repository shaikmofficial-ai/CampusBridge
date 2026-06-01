package com.mgr.campusbridge.service;

import com.mgr.campusbridge.entity.*;
import com.mgr.campusbridge.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ReportRepository reportRepository;

    public Map<String, Object> getAdminStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("pendingVerifications", userRepository.countByVerified(false));
        stats.put("openReports", reportRepository.countByStatus(Report.ReportStatus.OPEN));
        stats.put("activeThisWeek", userRepository.countByActive(true));
        return stats;
    }

    public List<User> getPendingVerifications() {
        return userRepository.findAll().stream()
                .filter(u -> !u.isVerified()).toList();
    }

    public User approveUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setVerified(true);
        return userRepository.save(user);
    }

    public void rejectUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(false);
        userRepository.save(user);
    }

    public List<Report> getOpenReports() {
        return reportRepository.findByStatus(Report.ReportStatus.OPEN);
    }

    public Report reviewReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        report.setStatus(Report.ReportStatus.REVIEWED);
        return reportRepository.save(report);
    }
}