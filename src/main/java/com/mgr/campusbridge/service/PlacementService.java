package com.mgr.campusbridge.service;

import com.mgr.campusbridge.dto.request.PlacementDriveRequest;
import com.mgr.campusbridge.dto.request.PlacementStoryRequest;
import com.mgr.campusbridge.dto.response.PlacementDriveResponse;
import com.mgr.campusbridge.dto.response.PlacementStoryResponse;
import com.mgr.campusbridge.entity.PlacementDrive;
import com.mgr.campusbridge.entity.PlacementStory;
import com.mgr.campusbridge.entity.User;
import com.mgr.campusbridge.exception.ResourceNotFoundException;
import com.mgr.campusbridge.repository.PlacementDriveRepository;
import com.mgr.campusbridge.repository.PlacementStoryRepository;
import com.mgr.campusbridge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlacementService {

    private final PlacementDriveRepository driveRepository;
    private final PlacementStoryRepository storyRepository;
    private final UserRepository userRepository;

    // --- DRIVES ---

    public List<PlacementDriveResponse> getAllDrives() {
        return driveRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(PlacementDriveResponse::from).collect(Collectors.toList());
    }

    public List<PlacementDriveResponse> getOpenDrives() {
        return driveRepository.findByStatusOrderByApplicationDeadlineAsc(PlacementDrive.DriveStatus.OPEN)
                .stream().map(PlacementDriveResponse::from).collect(Collectors.toList());
    }

    public PlacementDriveResponse getDriveById(Long id) {
        return PlacementDriveResponse.from(findDriveById(id));
    }

    @Transactional
    public PlacementDriveResponse createDrive(String adminEmail, PlacementDriveRequest request) {
        User admin = findByEmail(adminEmail);
        PlacementDrive drive = PlacementDrive.builder()
                .companyName(request.getCompanyName())
                .role(request.getRole())
                .packageAmount(request.getPackageAmount())
                .location(request.getLocation())
                .eligibilityCriteria(request.getEligibilityCriteria())
                .applicationDeadline(request.getApplicationDeadline())
                .applicationLink(request.getApplicationLink())
                .description(request.getDescription())
                .createdBy(admin)
                .build();
        return PlacementDriveResponse.from(driveRepository.save(drive));
    }

    @Transactional
    public PlacementDriveResponse updateDrive(Long id, PlacementDriveRequest request) {
        PlacementDrive drive = findDriveById(id);
        drive.setCompanyName(request.getCompanyName());
        drive.setRole(request.getRole());
        drive.setPackageAmount(request.getPackageAmount());
        drive.setLocation(request.getLocation());
        drive.setEligibilityCriteria(request.getEligibilityCriteria());
        drive.setApplicationDeadline(request.getApplicationDeadline());
        drive.setApplicationLink(request.getApplicationLink());
        drive.setDescription(request.getDescription());
        return PlacementDriveResponse.from(driveRepository.save(drive));
    }

    @Transactional
    public void deleteDrive(Long id) {
        driveRepository.delete(findDriveById(id));
    }

    // --- STORIES ---

    public List<PlacementStoryResponse> getAllStories() {
        return storyRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(PlacementStoryResponse::from).collect(Collectors.toList());
    }

    public PlacementStoryResponse getStoryById(Long id) {
        return PlacementStoryResponse.from(findStoryById(id));
    }

    @Transactional
    public PlacementStoryResponse createStory(String userEmail, PlacementStoryRequest request) {
        User user = findByEmail(userEmail);
        PlacementStory story = PlacementStory.builder()
                .companyName(request.getCompanyName())
                .studentName(user.getName())
                .role(request.getRole())
                .packageAmount(request.getPackageAmount())
                .story(request.getStory())
                .imageUrl(user.getProfilePictureUrl())
                .postedBy(user)
                .build();
        return PlacementStoryResponse.from(storyRepository.save(story));
    }

    @Transactional
    public PlacementStoryResponse updateStory(Long id, PlacementStoryRequest request) {
        PlacementStory story = findStoryById(id);
        story.setCompanyName(request.getCompanyName());
        story.setRole(request.getRole());
        story.setPackageAmount(request.getPackageAmount());
        story.setStory(request.getStory());
        return PlacementStoryResponse.from(storyRepository.save(story));
    }

    @Transactional
    public void deleteStory(Long id) {
        storyRepository.delete(findStoryById(id));
    }

    private PlacementDrive findDriveById(Long id) {
        return driveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Placement drive not found: " + id));
    }

    private PlacementStory findStoryById(Long id) {
        return storyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Placement story not found: " + id));
    }

    private User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
}