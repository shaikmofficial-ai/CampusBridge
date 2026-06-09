package com.mgr.campusbridge.dto.response;

import lombok.Builder;
import lombok.Data;

/** Result of running/checking a code submission via Judge0. */
@Data
@Builder
public class CodeRunResponse {
    private boolean passed;
    private String stdout;
    private String stderr;
    private String compileOutput;
    private String status;       // e.g. "Accepted", "Compilation Error"
    private boolean pointsAwarded;
    private int pointsEarned;
    private Integer unlockedNextOrderId; // null if none / already unlocked
    private String message;
}
