package com.mgr.campusbridge.service;

import com.mgr.campusbridge.dto.response.MentorConnectionResponse;
import com.mgr.campusbridge.entity.MentorConnection;
import com.mgr.campusbridge.entity.User;
import com.mgr.campusbridge.exception.ResourceNotFoundException;
import com.mgr.campusbridge.exception.UnauthorizedException;
import com.mgr.campusbridge.repository.MentorConnectionRepository;
import com.mgr.campusbridge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MentorConnectionService {

    private final MentorConnectionRepository connectionRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public MentorConnectionResponse sendRequest(String studentEmail, Long mentorId) {
        User student = findByEmail(studentEmail);
        User mentor = findById(mentorId);

        if (mentor.getRole() != User.Role.MENTOR && mentor.getRole() != User.Role.ALUMNI) {
            throw new UnauthorizedException("Target user is not a mentor");
        }
        if (student.getId().equals(mentor.getId())) {
            throw new UnauthorizedException("Cannot connect to yourself");
        }
        if (connectionRepository.existsByStudentAndMentorAndStatus(
                student, mentor, MentorConnection.Status.PENDING)) {
            throw new UnauthorizedException("Connection request already sent");
        }
        if (connectionRepository.existsByStudentAndMentorAndStatus(
                student, mentor, MentorConnection.Status.ACCEPTED)) {
            throw new UnauthorizedException("Already connected");
        }

        MentorConnection connection = MentorConnection.builder()
                .student(student)
                .mentor(mentor)
                .status(MentorConnection.Status.PENDING)
                .build();
        MentorConnection saved = connectionRepository.save(connection);

        notificationService.createNotification(
                mentor,
                "New Connection Request",
                student.getName() + " sent you a mentorship request",
                "CONNECTION_REQUEST"
        );

        return MentorConnectionResponse.from(saved);
    }

    @Transactional
    public MentorConnectionResponse acceptRequest(String mentorEmail, Long requestId) {
        MentorConnection connection = findConnectionById(requestId);
        validateMentorAction(mentorEmail, connection);

        connection.setStatus(MentorConnection.Status.ACCEPTED);
        connection.setRespondedAt(LocalDateTime.now());
        MentorConnection saved = connectionRepository.save(connection);

        notificationService.createNotification(
                connection.getStudent(),
                "Connection Accepted",
                connection.getMentor().getName() + " accepted your mentorship request",
                "CONNECTION_ACCEPTED"
        );

        return MentorConnectionResponse.from(saved);
    }

    @Transactional
    public MentorConnectionResponse rejectRequest(String mentorEmail, Long requestId) {
        MentorConnection connection = findConnectionById(requestId);
        validateMentorAction(mentorEmail, connection);

        connection.setStatus(MentorConnection.Status.REJECTED);
        connection.setRespondedAt(LocalDateTime.now());
        MentorConnection saved = connectionRepository.save(connection);

        notificationService.createNotification(
                connection.getStudent(),
                "Connection Update",
                connection.getMentor().getName() + " has reviewed your request",
                "CONNECTION_REJECTED"
        );

        return MentorConnectionResponse.from(saved);
    }

    public List<MentorConnectionResponse> getConnectedMentors(String studentEmail) {
        User student = findByEmail(studentEmail);
        return connectionRepository.findAcceptedConnectionsByStudent(student)
                .stream()
                .map(MentorConnectionResponse::from)
                .collect(Collectors.toList());
    }

    public List<MentorConnectionResponse> getPendingRequestsForMentor(String mentorEmail) {
        User mentor = findByEmail(mentorEmail);
        return connectionRepository.findPendingRequestsForMentor(mentor)
                .stream()
                .map(MentorConnectionResponse::from)
                .collect(Collectors.toList());
    }

    public List<MentorConnectionResponse> getSentPendingRequests(String studentEmail) {
        User student = findByEmail(studentEmail);
        return connectionRepository.findByStudentAndStatus(student, MentorConnection.Status.PENDING)
                .stream()
                .map(MentorConnectionResponse::from)
                .collect(Collectors.toList());
    }

    private void validateMentorAction(String email, MentorConnection connection) {
        if (!connection.getMentor().getEmail().equals(email)) {
            throw new UnauthorizedException("You are not authorized for this action");
        }
        if (connection.getStatus() != MentorConnection.Status.PENDING) {
            throw new UnauthorizedException("Request is no longer pending");
        }
    }

    private MentorConnection findConnectionById(Long id) {
        return connectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Connection request not found: " + id));
    }

    private User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }
}