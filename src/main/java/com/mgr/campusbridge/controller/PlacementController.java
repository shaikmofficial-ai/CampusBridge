package com.mgr.campusbridge.controller;

import com.mgr.campusbridge.entity.PlacementDrive;
import com.mgr.campusbridge.repository.UserRepository;
import com.mgr.campusbridge.service.PlacementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    private final UserRepository userRepository;

    @GetMapping("/drives")
    public ResponseEntity<?> getDrives() {
        return ResponseEntity.ok(placementService.getAllDrives());
    }

    @GetMapping("/drives/active")
    public ResponseEntity<?> getActiveDrives() {
        return ResponseEntity.ok(placementService.getActiveDrives());
    }

    @GetMapping("/stories")
    public ResponseEntity<?> getStories() {
        return ResponseEntity.ok(placementService.getStories());
    }

    @PostMapping("/drives")
    public ResponseEntity<?> createDrive(@RequestBody PlacementDrive drive) {
        return ResponseEntity.ok(placementService.createDrive(drive));
    }

    @PostMapping("/stories")
    public ResponseEntity<?> createStory(@AuthenticationPrincipal UserDetails userDetails,
                                         @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(placementService.createStory(
                userDetails.getUsername(), body.get("company"),
                body.get("story"), userRepository));
    }
}