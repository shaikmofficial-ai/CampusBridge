package com.mgr.campusbridge.controller;

import com.mgr.campusbridge.dto.request.PlacementDriveRequest;
import com.mgr.campusbridge.dto.request.PlacementStoryRequest;
import com.mgr.campusbridge.service.JobFetchService;
import com.mgr.campusbridge.service.PlacementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/placements")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PlacementController {

    private final PlacementService placementService;
    private final JobFetchService jobFetchService;

    // --- DRIVES ---

    @GetMapping("/drives")
    public ResponseEntity<?> getAllDrives() {
        return ResponseEntity.ok(placementService.getAllDrives());
    }

    @GetMapping("/drives/open")
    public ResponseEntity<?> getOpenDrives() {
        return ResponseEntity.ok(placementService.getOpenDrives());
    }

    @GetMapping("/drives/{id}")
    public ResponseEntity<?> getDriveById(@PathVariable Long id) {
        return ResponseEntity.ok(placementService.getDriveById(id));
    }

    @PostMapping("/drives")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createDrive(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PlacementDriveRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(placementService.createDrive(userDetails.getUsername(), request));
    }

    @PutMapping("/drives/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateDrive(
            @PathVariable Long id,
            @Valid @RequestBody PlacementDriveRequest request) {
        return ResponseEntity.ok(placementService.updateDrive(id, request));
    }

    @DeleteMapping("/drives/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteDrive(@PathVariable Long id) {
        placementService.deleteDrive(id);
        return ResponseEntity.noContent().build();
    }

    // --- STORIES ---

    @GetMapping("/stories")
    public ResponseEntity<?> getAllStories() {
        return ResponseEntity.ok(placementService.getAllStories());
    }

    @GetMapping("/stories/{id}")
    public ResponseEntity<?> getStoryById(@PathVariable Long id) {
        return ResponseEntity.ok(placementService.getStoryById(id));
    }

    @PostMapping("/stories")
    public ResponseEntity<?> createStory(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PlacementStoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(placementService.createStory(userDetails.getUsername(), request));
    }

    @PutMapping("/stories/{id}")
    public ResponseEntity<?> updateStory(
            @PathVariable Long id,
            @Valid @RequestBody PlacementStoryRequest request) {
        return ResponseEntity.ok(placementService.updateStory(id, request));
    }

    @DeleteMapping("/stories/{id}")
    public ResponseEntity<?> deleteStory(@PathVariable Long id) {
        placementService.deleteStory(id);
        return ResponseEntity.noContent().build();
    }

    // --- EXTERNAL JOBS (auto-fetched from Adzuna) ---

    /** Live job openings pulled from the external provider, cached locally. */
    @GetMapping("/jobs")
    public ResponseEntity<?> getExternalJobs(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String location) {
        return ResponseEntity.ok(jobFetchService.getJobs(query, location));
    }

    /** Manually trigger a refresh from the provider (admin only). */
    @PostMapping("/jobs/refresh")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> refreshExternalJobs() {
        int count = jobFetchService.refreshAll();
        return ResponseEntity.ok(Map.of("refreshed", count));
    }
}