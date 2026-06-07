package com.mgr.campusbridge.controller;

import com.mgr.campusbridge.service.JobFetchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Internal job board endpoints. Serves instantly from the locally cached
 * records maintained by the background {@code JobAutomationService} — students
 * never wait on the external Adzuna network call.
 */
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class JobController {

    private final JobFetchService jobFetchService;

    /** Cached job listings, optionally filtered by keyword/location. */
    @GetMapping
    public ResponseEntity<?> getJobs(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String location) {
        return ResponseEntity.ok(jobFetchService.getJobs(query, location));
    }

    /** Manually trigger a fresh fetch + cache replace (admin only). */
    @PostMapping("/refresh")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> refresh() {
        return ResponseEntity.ok(Map.of("refreshed", jobFetchService.refreshAllReplacing()));
    }
}
