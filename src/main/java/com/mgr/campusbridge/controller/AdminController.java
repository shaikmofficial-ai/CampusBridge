package com.mgr.campusbridge.controller;

import com.mgr.campusbridge.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        return ResponseEntity.ok(adminService.getAdminStats());
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getPending() {
        return ResponseEntity.ok(adminService.getPendingVerifications());
    }

    @PostMapping("/approve/{userId}")
    public ResponseEntity<?> approve(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.approveUser(userId));
    }

    @PostMapping("/reject/{userId}")
    public ResponseEntity<?> reject(@PathVariable Long userId) {
        adminService.rejectUser(userId);
        return ResponseEntity.ok("User rejected");
    }

    @GetMapping("/reports")
    public ResponseEntity<?> getReports() {
        return ResponseEntity.ok(adminService.getOpenReports());
    }

    @PostMapping("/reports/{id}/review")
    public ResponseEntity<?> reviewReport(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.reviewReport(id));
    }
}