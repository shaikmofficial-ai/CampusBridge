package com.mgr.campusbridge.service;

import com.mgr.campusbridge.dto.request.ProfileUpdateRequest;
import com.mgr.campusbridge.entity.User;
import com.mgr.campusbridge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;

    public User getProfile(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User getProfileById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User updateProfile(String email, ProfileUpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setBio(request.getBio());
        user.setLocation(request.getLocation());
        user.setLinkedinUrl(request.getLinkedinUrl());
        user.setGithubUrl(request.getGithubUrl());
        user.setSkills(request.getSkills());
        user.setAchievements(request.getAchievements());
        return userRepository.save(user);
    }
}