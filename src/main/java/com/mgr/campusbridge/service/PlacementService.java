package com.mgr.campusbridge.service;

import com.mgr.campusbridge.entity.*;
import com.mgr.campusbridge.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlacementService {

    private final PlacementDriveRepository driveRepository;
    private final PlacementStoryRepository storyRepository;

    public List<PlacementDrive> getActiveDrives() {
        return driveRepository.findByStatus(PlacementDrive.DriveStatus.OPEN);
    }

    public List<PlacementDrive> getAllDrives() {
        return driveRepository.findAll();
    }

    public List<PlacementStory> getStories() {
        return storyRepository.findAllByOrderByPostedAtDesc();
    }

    public PlacementDrive createDrive(PlacementDrive drive) {
        return driveRepository.save(drive);
    }

    public PlacementStory createStory(String email, String company, String story,
                                      UserRepository userRepository) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return storyRepository.save(PlacementStory.builder()
                .user(user)
                .companyName(company)
                .story(story)
                .excerpt(story.length() > 100 ? story.substring(0, 100) + "..." : story)
                .build());
    }
}