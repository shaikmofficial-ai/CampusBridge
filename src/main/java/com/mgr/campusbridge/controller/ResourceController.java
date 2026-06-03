package com.mgr.campusbridge.controller;

import com.mgr.campusbridge.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ResourceController {

    private final ResourceService resourceService;

    @GetMapping
    public ResponseEntity<?> getAll(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String type) {
        if (type != null && !type.isBlank()) {
            return ResponseEntity.ok(resourceService.getByType(userDetails.getUsername(), type));
        }
        return ResponseEntity.ok(resourceService.getAllResources(userDetails.getUsername()));
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam String department,
            @RequestParam String type,
            @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(resourceService.uploadResource(
                        userDetails.getUsername(), title, description, department, type, file));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<FileSystemResource> download(@PathVariable Long id) {
        FileSystemResource file = resourceService.downloadResource(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }

    @PostMapping("/{id}/save")
    public ResponseEntity<?> save(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        resourceService.saveResource(userDetails.getUsername(), id);
        return ResponseEntity.ok("Resource saved");
    }

    @DeleteMapping("/{id}/save")
    public ResponseEntity<?> unsave(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        resourceService.unsaveResource(userDetails.getUsername(), id);
        return ResponseEntity.ok("Resource unsaved");
    }

    @GetMapping("/saved")
    public ResponseEntity<?> getSaved(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(resourceService.getSavedResources(userDetails.getUsername()));
    }
}