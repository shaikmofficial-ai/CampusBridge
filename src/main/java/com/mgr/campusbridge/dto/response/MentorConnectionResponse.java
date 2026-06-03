package com.mgr.campusbridge.dto.response;

import com.mgr.campusbridge.entity.MentorConnection;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class MentorConnectionResponse {
    private Long id;
    private Long mentorId;
    private String mentorName;
    private String mentorEmail;
    private String mentorProfilePicture;
    private String mentorDepartment;
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private String status;
    private LocalDateTime requestedAt;
    private LocalDateTime respondedAt;

    public static MentorConnectionResponse from(MentorConnection c) {
        return MentorConnectionResponse.builder()
                .id(c.getId())
                .mentorId(c.getMentor().getId())
                .mentorName(c.getMentor().getName())
                .mentorEmail(c.getMentor().getEmail())
                .mentorProfilePicture(c.getMentor().getProfilePictureUrl())
                .mentorDepartment(c.getMentor().getDepartment())
                .studentId(c.getStudent().getId())
                .studentName(c.getStudent().getName())
                .studentEmail(c.getStudent().getEmail())
                .status(c.getStatus().name())
                .requestedAt(c.getRequestedAt())
                .respondedAt(c.getRespondedAt())
                .build();
    }
}