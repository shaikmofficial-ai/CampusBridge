package com.mgr.campusbridge.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Minimal mapping of the Adzuna "search" endpoint response.
 * See https://developer.adzuna.com/docs/search
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdzunaSearchResponse {

    private long count;
    private List<AdzunaJob> results;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AdzunaJob {
        private String id;
        private String title;
        private String description;

        @JsonProperty("redirect_url")
        private String redirectUrl;

        /** ISO-8601 timestamp, e.g. "2024-05-01T10:00:00Z". */
        private String created;

        @JsonProperty("salary_min")
        private Double salaryMin;

        @JsonProperty("salary_max")
        private Double salaryMax;

        @JsonProperty("contract_time")
        private String contractTime;

        private AdzunaCompany company;
        private AdzunaLocation location;
        private AdzunaCategory category;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AdzunaCompany {
        @JsonProperty("display_name")
        private String displayName;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AdzunaLocation {
        @JsonProperty("display_name")
        private String displayName;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AdzunaCategory {
        private String label;
    }
}
