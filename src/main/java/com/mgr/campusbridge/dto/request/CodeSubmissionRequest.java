package com.mgr.campusbridge.dto.request;

import lombok.Data;

@Data
public class CodeSubmissionRequest {
    /** orderIndex of the module being attempted. */
    private int moduleOrderId;
    private String code;
    /** Optional override; defaults to the module's configured language. */
    private Integer languageId;
}
