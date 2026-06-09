package com.mgr.campusbridge.dto.response;

import com.mgr.campusbridge.entity.ProfileSnapshot;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class SnapshotResponse {
    private Long id;
    private LocalDate date;
    private int score;

    public static SnapshotResponse from(ProfileSnapshot s) {
        return SnapshotResponse.builder()
                .id(s.getId())
                .date(s.getRecordedDate())
                .score(s.getScore())
                .build();
    }
}
