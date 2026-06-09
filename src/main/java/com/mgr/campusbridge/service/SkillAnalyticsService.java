package com.mgr.campusbridge.service;

import com.mgr.campusbridge.dto.response.SnapshotResponse;
import com.mgr.campusbridge.entity.ProfileSnapshot;
import com.mgr.campusbridge.entity.User;
import com.mgr.campusbridge.exception.ResourceNotFoundException;
import com.mgr.campusbridge.repository.ForumPostRepository;
import com.mgr.campusbridge.repository.ProfileSnapshotRepository;
import com.mgr.campusbridge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Computes a student's Placement Readiness Index (PRI) and records historical
 * snapshots so growth can be graphed over time.
 *
 *   PRI = (skill tags * 5) + (verified certificates * 25) + (forum posts * 2)
 */
@Service
@RequiredArgsConstructor
public class SkillAnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(SkillAnalyticsService.class);

    private final ProfileSnapshotRepository snapshotRepository;
    private final ForumPostRepository forumPostRepository;
    private final UserRepository userRepository;

    /** Weighted PRI formula. */
    public int computePri(User user) {
        int skillTags = user.getSkills() != null ? user.getSkills().size() : 0;
        // Verified certificates are tracked via the user's achievements list once
        // the account is APPROVED (admin-verified). Pending accounts count 0.
        int verifiedCertificates = (user.getAccountStatus() == User.AccountStatus.APPROVED
                && user.getAchievements() != null) ? user.getAchievements().size() : 0;
        long forumPosts = forumPostRepository.countByAuthorId(user.getId());

        return (skillTags * 5) + (verifiedCertificates * 25) + (int) (forumPosts * 2);
    }

    /**
     * Recompute the user's PRI and upsert today's snapshot row. Called whenever
     * a student edits their profile, adds a skill, posts to the forum, or an
     * admin verifies their certificates. Best-effort: never throws to callers.
     */
    @Transactional
    public void recordSnapshot(User user) {
        if (user == null || user.getId() == null) return;
        // Only meaningful for students.
        if (user.getRole() != User.Role.STUDENT) return;
        try {
            int score = computePri(user);
            LocalDate today = LocalDate.now();
            ProfileSnapshot snapshot = snapshotRepository
                    .findByUser_IdAndRecordedDate(user.getId(), today)
                    .orElseGet(() -> ProfileSnapshot.builder().user(user).recordedDate(today).build());
            snapshot.setScore(score);
            snapshotRepository.save(snapshot);
        } catch (Exception e) {
            log.warn("Failed to record PRI snapshot for user {}: {}", user.getId(), e.getMessage());
        }
    }

    /** Convenience overload that loads the user by email first. */
    @Transactional
    public void recordSnapshotByEmail(String email) {
        userRepository.findByEmail(email).ifPresent(this::recordSnapshot);
    }

    /** Snapshot log, oldest -> newest, for the growth chart. */
    public List<SnapshotResponse> getSnapshots(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found: " + userId);
        }
        return snapshotRepository.findByUser_IdOrderByRecordedDateAsc(userId)
                .stream().map(SnapshotResponse::from).toList();
    }
}
