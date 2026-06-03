package com.mgr.campusbridge.controller;

import com.mgr.campusbridge.dto.response.MentorConnectionResponse;
import com.mgr.campusbridge.service.MentorConnectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mentors")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MentorConnectionController {

    private final MentorConnectionService connectionService;

    @PostMapping("/{mentorId}/request")
    public ResponseEntity<MentorConnectionResponse> sendRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(connectionService.sendRequest(userDetails.getUsername(), id));
    }

    @PostMapping("/requests/{requestId}/accept")
    public ResponseEntity<MentorConnectionResponse> accept(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long requestId) {
        return ResponseEntity.ok(connectionService.acceptRequest(userDetails.getUsername(), requestId));
    }

    @PostMapping("/requests/{requestId}/reject")
    public ResponseEntity<MentorConnectionResponse> reject(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long requestId) {
        return ResponseEntity.ok(connectionService.rejectRequest(userDetails.getUsername(), requestId));
    }

    @GetMapping("/connections")
    public ResponseEntity<List<MentorConnectionResponse>> getConnections(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(connectionService.getConnectedMentors(userDetails.getUsername()));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<MentorConnectionResponse>> getPending(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(connectionService.getPendingRequestsForMentor(userDetails.getUsername()));
    }

    @GetMapping("/sent")
    public ResponseEntity<List<MentorConnectionResponse>> getSent(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(connectionService.getSentPendingRequests(userDetails.getUsername()));
    }
}