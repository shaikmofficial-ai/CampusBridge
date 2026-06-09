package com.mgr.campusbridge.controller;

import com.mgr.campusbridge.service.SkillAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Time-series skill tracking analytics (Placement Readiness Index history).
 */
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private final SkillAnalyticsService skillAnalyticsService;

    /** A user's PRI snapshot log, oldest -> newest, for the growth chart. */
    @GetMapping("/snapshots/{userId}")
    public ResponseEntity<?> getSnapshots(@PathVariable Long userId) {
        return ResponseEntity.ok(skillAnalyticsService.getSnapshots(userId));
    }
}
