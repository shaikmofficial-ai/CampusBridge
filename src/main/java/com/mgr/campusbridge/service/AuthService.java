package com.mgr.campusbridge.service;

import com.mgr.campusbridge.dto.request.LoginRequest;
import com.mgr.campusbridge.dto.request.RegisterRequest;
import com.mgr.campusbridge.dto.response.AuthResponse;
import com.mgr.campusbridge.entity.MentorProfile;
import com.mgr.campusbridge.entity.User;
import com.mgr.campusbridge.repository.MentorProfileRepository;
import com.mgr.campusbridge.repository.UserRepository;
import com.mgr.campusbridge.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final MentorProfileRepository mentorProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        if (request.getRegisterNumber() != null && !request.getRegisterNumber().isBlank()
                && userRepository.existsByRegisterNumber(request.getRegisterNumber().trim())) {
            throw new RuntimeException("Register number already registered");
        }

        // Security: admins are never created via public registration. They are
        // inserted directly into the database. Only STUDENT/MENTOR/ALUMNI allowed.
        User.Role requestedRole;
        try {
            requestedRole = User.Role.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new RuntimeException("Invalid role");
        }
        if (requestedRole == User.Role.ADMIN) {
            throw new RuntimeException("Admin accounts cannot be created through registration.");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(requestedRole)
                .registerNumber(request.getRegisterNumber() != null && !request.getRegisterNumber().isBlank()
                        ? request.getRegisterNumber().trim() : null)
                .department(request.getDepartment())
                .batch(request.getBatch())
                .verified(false)
                .active(true)
                .communityPoints(0)
                .accountStatus(User.AccountStatus.PENDING)
                .build();

        User savedUser = userRepository.save(user);

        // Auto-create mentor profile for mentors
        if (savedUser.getRole() == User.Role.MENTOR) {

            MentorProfile mentorProfile = MentorProfile.builder()
                    .user(savedUser)
                    .company("")
                    .designation("")
                    .rating(0.0)
                    .reviewCount(0)
                    .available(true)
                    .build();

            mentorProfileRepository.save(mentorProfile);
        }

        String token = jwtUtil.generateToken(
                savedUser.getEmail(),
                savedUser.getRole().name()
        );

        return new AuthResponse(
                token,
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole().name(),
                savedUser.getRegisterNumber()
        );
    }

    public AuthResponse login(LoginRequest request) {

        String id = (request.getIdentifier() != null && !request.getIdentifier().isBlank())
                ? request.getIdentifier().trim()
                : request.getEmail();

        User user = userRepository.findByEmail(id)
                .or(() -> userRepository.findByRegisterNumber(id))
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getRole().name()
        );

        return new AuthResponse(
                token,
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.getRegisterNumber()
        );
    }
}