package com.mgr.campusbridge.service;

import com.mgr.campusbridge.dto.request.CodeSubmissionRequest;
import com.mgr.campusbridge.dto.response.CodeRunResponse;
import com.mgr.campusbridge.dto.response.LearningNodeResponse;
import com.mgr.campusbridge.entity.LearningModule;
import com.mgr.campusbridge.entity.User;
import com.mgr.campusbridge.entity.UserLearningProgress;
import com.mgr.campusbridge.exception.ResourceNotFoundException;
import com.mgr.campusbridge.repository.LearningModuleRepository;
import com.mgr.campusbridge.repository.UserLearningProgressRepository;
import com.mgr.campusbridge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LearningService {

    private static final int POINTS_PER_LESSON = 25;

    private final LearningModuleRepository moduleRepository;
    private final UserLearningProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final Judge0Service judge0Service;
    private final SkillAnalyticsService skillAnalyticsService;

    /** The roadmap with per-user node states. */
    public List<LearningNodeResponse> getRoadmap(String email) {
        User user = findByEmail(email);
        Set<Integer> completed = progressRepository.findByUserId(user.getId()).stream()
                .map(UserLearningProgress::getModuleOrderId).collect(Collectors.toSet());

        List<LearningModule> modules = moduleRepository.findAllByOrderByOrderIndexAsc();
        // The active node is the first incomplete one.
        int activeOrder = modules.stream()
                .map(LearningModule::getOrderIndex)
                .filter(o -> !completed.contains(o))
                .min(Integer::compareTo)
                .orElse(Integer.MAX_VALUE);

        return modules.stream().map(m -> {
            String state;
            if (completed.contains(m.getOrderIndex())) state = "COMPLETED";
            else if (m.getOrderIndex() == activeOrder) state = "ACTIVE";
            else state = "LOCKED";
            return LearningNodeResponse.from(m, state);
        }).collect(Collectors.toList());
    }

    /**
     * Run + verify a submission. On success: mark complete, +25 points, unlock
     * the next node, and record a PRI snapshot.
     */
    @Transactional
    public CodeRunResponse submit(String email, CodeSubmissionRequest request) {
        User user = findByEmail(email);
        LearningModule module = moduleRepository.findByOrderIndex(request.getModuleOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found: " + request.getModuleOrderId()));

        // Guard: the node must be unlocked (previous completed) before attempting.
        if (request.getModuleOrderId() > 1
                && !progressRepository.existsByUserIdAndModuleOrderId(user.getId(), request.getModuleOrderId() - 1)) {
            return CodeRunResponse.builder()
                    .passed(false)
                    .status("Locked")
                    .message("Complete the previous challenge to unlock this module!")
                    .build();
        }

        int languageId = request.getLanguageId() != null ? request.getLanguageId() : module.getLanguageId();
        Judge0Service.ExecResult exec = judge0Service.execute(request.getCode(), languageId, null);

        String stdout = exec.getStdout();
        boolean matched = stdout != null && module.getExpectedOutput() != null
                && stdout.trim().equals(module.getExpectedOutput().trim());

        CodeRunResponse.CodeRunResponseBuilder out = CodeRunResponse.builder()
                .stdout(stdout)
                .stderr(exec.getStderr())
                .compileOutput(exec.getCompileOutput())
                .status(exec.getStatus())
                .passed(matched);

        if (!matched) {
            out.message(exec.isConfigured()
                    ? "Output doesn't match yet. Check your logic and try again."
                    : "Code execution isn't configured on the server.");
            return out.build();
        }

        // ---- Success path ----
        boolean alreadyDone = progressRepository.existsByUserIdAndModuleOrderId(user.getId(), module.getOrderIndex());
        if (!alreadyDone) {
            progressRepository.save(UserLearningProgress.builder()
                    .userId(user.getId())
                    .moduleOrderId(module.getOrderIndex())
                    .build());

            user.setCommunityPoints(user.getCommunityPoints() + POINTS_PER_LESSON);
            userRepository.save(user);

            // Force the PRI timeline to update.
            skillAnalyticsService.recordSnapshot(user);

            out.pointsAwarded(true).pointsEarned(POINTS_PER_LESSON);
        } else {
            out.pointsAwarded(false).pointsEarned(0);
        }

        // Next node (if any) is now unlocked.
        moduleRepository.findByOrderIndex(module.getOrderIndex() + 1)
                .ifPresent(next -> out.unlockedNextOrderId(next.getOrderIndex()));

        out.message(alreadyDone
                ? "Correct! You'd already cleared this one."
                : "Challenge Solved! +" + POINTS_PER_LESSON + " Points Added to Your Rank!");
        return out.build();
    }

    private User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
}
