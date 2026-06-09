package com.mgr.campusbridge.controller;

import com.mgr.campusbridge.dto.response.LeaderboardEntryResponse;
import com.mgr.campusbridge.entity.User;
import com.mgr.campusbridge.repository.UserLearningProgressRepository;
import com.mgr.campusbridge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * Campus Toppers leaderboard — aggregates active users by total points and
 * lessons solved, sorted descending.
 */
@RestController
@RequestMapping("/api/leaderboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LeaderboardController {

    private final UserRepository userRepository;
    private final UserLearningProgressRepository progressRepository;

    @GetMapping("/global")
    public ResponseEntity<?> global() {
        // lessons solved per user
        Map<Long, Long> lessons = new HashMap<>();
        for (Object[] row : progressRepository.countLessonsPerUser()) {
            lessons.put((Long) row[0], (Long) row[1]);
        }

        List<User> users = userRepository.findByAccountStatus(User.AccountStatus.APPROVED);

        List<LeaderboardEntryResponse> ranked = users.stream()
                // exclude banned + admins from the competitive board
                .filter(u -> !"BANNED".equalsIgnoreCase(u.getAccountState()))
                .filter(u -> u.getRole() != User.Role.ADMIN)
                .sorted(Comparator
                        .comparingInt(User::getCommunityPoints).reversed()
                        .thenComparing(u -> lessons.getOrDefault(u.getId(), 0L), Comparator.reverseOrder()))
                .map(u -> LeaderboardEntryResponse.builder()
                        .userId(u.getId())
                        .name(u.getName())
                        .department(u.getDepartment())
                        .batch(u.getBatch())
                        .profilePictureUrl(u.getProfilePictureUrl())
                        .lessonsSolved(lessons.getOrDefault(u.getId(), 0L))
                        .totalPoints(u.getCommunityPoints())
                        .build())
                .collect(Collectors.toList());

        // assign 1-based rank
        for (int i = 0; i < ranked.size(); i++) {
            ranked.get(i).setRank(i + 1);
        }
        return ResponseEntity.ok(ranked);
    }
}
