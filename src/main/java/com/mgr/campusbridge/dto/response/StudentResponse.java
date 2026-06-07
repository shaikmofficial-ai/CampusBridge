package com.mgr.campusbridge.dto.response;

import com.mgr.campusbridge.entity.User;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/** A student as seen by mentors/alumni (discovery + connected-student selector). */
@Data
@Builder
public class StudentResponse {
    private Long id;
    private String name;
    private String email;
    private String registerNumber;
    private String department;
    private String batch;
    private String bio;
    private List<String> skills;
    private String profilePictureUrl;

    public static StudentResponse from(User u) {
        return StudentResponse.builder()
                .id(u.getId())
                .name(u.getName())
                .email(u.getEmail())
                .registerNumber(u.getRegisterNumber())
                .department(u.getDepartment())
                .batch(u.getBatch())
                .bio(u.getBio())
                .skills(u.getSkills())
                .profilePictureUrl(u.getProfilePictureUrl())
                .build();
    }
}
