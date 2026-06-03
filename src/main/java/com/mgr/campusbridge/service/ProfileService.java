package com.mgr.campusbridge.service;

import com.mgr.campusbridge.dto.request.ProfileUpdateRequest;
import com.mgr.campusbridge.dto.response.ProfileResponse;
import com.mgr.campusbridge.entity.User;
import com.mgr.campusbridge.exception.ResourceNotFoundException;
import com.mgr.campusbridge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;

    public ProfileResponse getProfile(String email) {
        User user = findByEmail(email);
        return ProfileResponse.from(user);
    }

    public ProfileResponse getProfileById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return ProfileResponse.from(user);
    }

    @Transactional
    public ProfileResponse updateProfile(String email, ProfileUpdateRequest request) {
        User user = findByEmail(email);
        if (request.getBio() != null) user.setBio(request.getBio());
        if (request.getLinkedinUrl() != null) user.setLinkedinUrl(request.getLinkedinUrl());
        if (request.getGithubUrl() != null) user.setGithubUrl(request.getGithubUrl());
        if (request.getPortfolioUrl() != null) user.setPortfolioUrl(request.getPortfolioUrl());
        if (request.getSkills() != null) user.setSkills(request.getSkills());
        if (request.getAchievements() != null) user.setAchievements(request.getAchievements());
        return ProfileResponse.from(userRepository.save(user));
    }

    @Transactional
    public ProfileResponse updateProfilePicture(String email, String pictureUrl) {
        User user = findByEmail(email);
        user.setProfilePictureUrl(pictureUrl);
        return ProfileResponse.from(userRepository.save(user));
    }

    private User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
}