package com.mgr.campusbridge.controller;

import com.mgr.campusbridge.dto.request.CodeSubmissionRequest;
import com.mgr.campusbridge.service.LearningService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/learning")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LearningController {

    private final LearningService learningService;

    /** The roadmap with the caller's locked/active/completed node states. */
    @GetMapping("/roadmap")
    public ResponseEntity<?> roadmap(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(learningService.getRoadmap(userDetails.getUsername()));
    }

    /** Run + verify a code submission; awards points on success. */
    @PostMapping("/submit")
    public ResponseEntity<?> submit(@AuthenticationPrincipal UserDetails userDetails,
                                    @RequestBody CodeSubmissionRequest request) {
        return ResponseEntity.ok(learningService.submit(userDetails.getUsername(), request));
    }
}
