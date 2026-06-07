package com.mgr.campusbridge.service;

import com.mgr.campusbridge.dto.request.MentorPlacementRequest;
import com.mgr.campusbridge.dto.request.MentorProfileRequest;
import com.mgr.campusbridge.dto.response.MentorPlacementResponse;
import com.mgr.campusbridge.dto.response.MentorResponse;
import com.mgr.campusbridge.dto.response.StudentResponse;
import com.mgr.campusbridge.entity.*;
import com.mgr.campusbridge.exception.ResourceNotFoundException;
import com.mgr.campusbridge.exception.UnauthorizedException;
import com.mgr.campusbridge.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MentorService {

    private final MentorProfileRepository mentorProfileRepository;
    private final MentorConnectionRepository connectionRepository;
    private final MentorPlacementRepository placementRepository;
    private final UserRepository userRepository;

    public List<MentorResponse> getAllMentors(String domain, String keyword) {
        return mentorProfileRepository.searchMentors(domain, keyword)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    /** Read a single mentor's profile by their user id. */
    public MentorResponse getMentorByUserId(Long userId) {
        MentorProfile mp = mentorProfileRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Mentor profile not found for user: " + userId));
        return toResponse(mp);
    }

    public String sendConnectionRequest(String studentEmail, Long mentorUserId) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        User mentor = userRepository.findById(mentorUserId)
                .orElseThrow(() -> new RuntimeException("Mentor not found"));

        // Prevent duplicate requests.
        if (connectionRepository.findByStudentAndMentor(student, mentor).isPresent()) {
            throw new UnauthorizedException("You have already sent a request to this mentor.");
        }

        MentorConnection connection = MentorConnection.builder()
                .student(student)
                .mentor(mentor)
                .status(MentorConnection.Status.PENDING)
                .build();
        connectionRepository.save(connection);
        return "Connection request sent";
    }

    /** Create or update the calling mentor's profile (incl. alumni fields). */
    @Transactional
    public MentorResponse upsertMyProfile(String email, MentorProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        if (user.getRole() != User.Role.MENTOR && user.getRole() != User.Role.ALUMNI
                && user.getRole() != User.Role.ADMIN) {
            throw new UnauthorizedException("Only mentors/alumni can edit a mentor profile.");
        }

        MentorProfile mp = mentorProfileRepository.findByUser_Id(user.getId())
                .orElseGet(() -> MentorProfile.builder().user(user).available(true).build());

        if (request.getDesignation() != null) mp.setDesignation(request.getDesignation());
        if (request.getCompany() != null) mp.setCompany(request.getCompany());
        if (request.getCurrentCompany() != null) mp.setCurrentCompany(request.getCurrentCompany());
        if (request.getCurrentRole() != null) mp.setCurrentRole(request.getCurrentRole());
        if (request.getSkills() != null) mp.setSkills(request.getSkills());
        if (request.getDomains() != null) mp.setDomains(request.getDomains());
        if (request.getAvailable() != null) mp.setAvailable(request.getAvailable());

        return toResponse(mentorProfileRepository.save(mp));
    }

    // --- Placement tracker ---

    public List<MentorPlacementResponse> getPlacements(Long mentorUserId) {
        return placementRepository.findByMentor_IdOrderByCreatedAtDesc(mentorUserId)
                .stream().map(MentorPlacementResponse::from).collect(Collectors.toList());
    }

    @Transactional
    public MentorPlacementResponse addPlacement(String mentorEmail, MentorPlacementRequest request) {
        User mentor = userRepository.findByEmail(mentorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + mentorEmail));
        if (mentor.getRole() != User.Role.MENTOR && mentor.getRole() != User.Role.ALUMNI
                && mentor.getRole() != User.Role.ADMIN) {
            throw new UnauthorizedException("Only mentors can add placement records.");
        }
        if (request.getCompany() == null || request.getCompany().isBlank()) {
            throw new UnauthorizedException("Company is required.");
        }
        // A placement must be tied to a student who has an ACCEPTED connection
        // with this mentor (no more free-typed names).
        if (request.getStudentId() == null) {
            throw new UnauthorizedException("Please select a connected student.");
        }
        User student = userRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + request.getStudentId()));

        boolean connected = mentor.getRole() == User.Role.ADMIN
                || connectionRepository.existsByStudentAndMentorAndStatus(
                        student, mentor, MentorConnection.Status.ACCEPTED);
        if (!connected) {
            throw new UnauthorizedException(
                    "You can only record placements for students connected with you.");
        }

        MentorPlacement placement = MentorPlacement.builder()
                .mentor(mentor)
                .student(student)
                .studentName(student.getName())
                .batch(request.getBatch() != null ? request.getBatch() : student.getBatch())
                .company(request.getCompany().trim())
                .role(request.getRole())
                .packageAmount(request.getPackageAmount())
                .build();

        return MentorPlacementResponse.from(placementRepository.save(placement));
    }

    /** Students with an ACCEPTED connection to the calling mentor (for the placement selector). */
    public List<StudentResponse> getMyConnectedStudents(String mentorEmail) {
        User mentor = userRepository.findByEmail(mentorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + mentorEmail));
        return connectionRepository.findAcceptedStudentsForMentor(mentor)
                .stream().map(StudentResponse::from).collect(Collectors.toList());
    }

    /** Student discovery for mentors/alumni, optionally filtered by skill/keyword. */
    public List<StudentResponse> discoverStudents(String skill, String keyword) {
        String s = (skill != null && !skill.isBlank()) ? skill.trim() : null;
        String k = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return userRepository.searchStudents(s, k)
                .stream().map(StudentResponse::from).collect(Collectors.toList());
    }

    @Transactional
    public void deletePlacement(String mentorEmail, Long placementId) {
        User user = userRepository.findByEmail(mentorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + mentorEmail));
        MentorPlacement placement = placementRepository.findById(placementId)
                .orElseThrow(() -> new ResourceNotFoundException("Placement not found: " + placementId));
        boolean owner = placement.getMentor() != null && placement.getMentor().getId().equals(user.getId());
        if (!owner && user.getRole() != User.Role.ADMIN) {
            throw new UnauthorizedException("You can only remove your own placement records.");
        }
        placementRepository.delete(placement);
    }

    private MentorResponse toResponse(MentorProfile mp) {
        MentorResponse r = new MentorResponse();
        r.setId(mp.getUser().getId());
        r.setName(mp.getUser().getName());
        r.setDesignation(mp.getDesignation());
        r.setCompany(mp.getCompany());
        r.setCurrentCompany(mp.getCurrentCompany());
        r.setCurrentRole(mp.getCurrentRole());
        r.setRating(mp.getRating());
        r.setReviewCount(mp.getReviewCount());
        r.setPlacedCount((int) placementRepository.countByMentor_Id(mp.getUser().getId()));
        r.setSkills(mp.getSkills());
        r.setDomains(mp.getDomains());
        r.setProfilePicture(mp.getUser().getProfilePictureUrl());
        return r;
    }
}
