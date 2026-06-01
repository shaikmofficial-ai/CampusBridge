package com.mgr.campusbridge.service;

import com.mgr.campusbridge.dto.response.MentorResponse;
import com.mgr.campusbridge.entity.*;
import com.mgr.campusbridge.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MentorService {

    private final MentorProfileRepository mentorProfileRepository;
    private final MentorConnectionRepository connectionRepository;
    private final UserRepository userRepository;

    public List<MentorResponse> getAllMentors(String domain, String keyword) {
        return mentorProfileRepository.searchMentors(domain, keyword)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public String sendConnectionRequest(String studentEmail, Long mentorUserId) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        User mentor = userRepository.findById(mentorUserId)
                .orElseThrow(() -> new RuntimeException("Mentor not found"));
        MentorConnection connection = MentorConnection.builder()
                .student(student)
                .mentor(mentor)
                .status(MentorConnection.Status.PENDING)
                .build();
        connectionRepository.save(connection);
        return "Connection request sent";
    }

    private MentorResponse toResponse(MentorProfile mp) {
        MentorResponse r = new MentorResponse();
        r.setId(mp.getUser().getId());
        r.setName(mp.getUser().getName());
        r.setDesignation(mp.getDesignation());
        r.setCompany(mp.getCompany());
        r.setRating(mp.getRating());
        r.setReviewCount(mp.getReviewCount());
        r.setSkills(mp.getSkills());
        r.setDomains(mp.getDomains());
        r.setProfilePicture(mp.getUser().getProfilePicture());
        return r;
    }
}