package com.mgr.campusbridge.service;

import com.mgr.campusbridge.dto.request.MentorJobRequest;
import com.mgr.campusbridge.dto.response.MentorJobResponse;
import com.mgr.campusbridge.entity.MentorJob;
import com.mgr.campusbridge.entity.User;
import com.mgr.campusbridge.exception.ResourceNotFoundException;
import com.mgr.campusbridge.exception.UnauthorizedException;
import com.mgr.campusbridge.repository.MentorJobRepository;
import com.mgr.campusbridge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MentorJobService {

    private final MentorJobRepository mentorJobRepository;
    private final UserRepository userRepository;

    public List<MentorJobResponse> getAll() {
        return mentorJobRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(MentorJobResponse::from).toList();
    }

    public MentorJobResponse create(String email, MentorJobRequest request) {
        User mentor = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        // Only mentors/alumni (and admins) may post job openings.
        if (mentor.getRole() != User.Role.MENTOR && mentor.getRole() != User.Role.ALUMNI
                && mentor.getRole() != User.Role.ADMIN) {
            throw new UnauthorizedException("Only mentors and alumni can post job openings.");
        }
        if (mentor.getRole() != User.Role.ADMIN && mentor.getAccountStatus() != User.AccountStatus.APPROVED) {
            throw new UnauthorizedException("Your account is pending admin approval.");
        }
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new UnauthorizedException("Job title is required.");
        }

        MentorJob job = MentorJob.builder()
                .title(request.getTitle().trim())
                .company(request.getCompany())
                .location(request.getLocation())
                .jobType(request.getJobType())
                .description(request.getDescription())
                .applyLink(request.getApplyLink())
                .skills(request.getSkills())
                .postedBy(mentor)
                .build();

        return MentorJobResponse.from(mentorJobRepository.save(job));
    }

    public void delete(String email, Long id) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        MentorJob job = mentorJobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + id));
        boolean owner = job.getPostedBy() != null && job.getPostedBy().getId().equals(user.getId());
        if (!owner && user.getRole() != User.Role.ADMIN) {
            throw new UnauthorizedException("You can only remove your own postings.");
        }
        mentorJobRepository.delete(job);
    }
}
