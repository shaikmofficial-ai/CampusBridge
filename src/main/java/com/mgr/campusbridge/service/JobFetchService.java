package com.mgr.campusbridge.service;

import com.mgr.campusbridge.dto.external.AdzunaSearchResponse;
import com.mgr.campusbridge.dto.response.ExternalJobResponse;
import com.mgr.campusbridge.entity.ExternalJob;
import com.mgr.campusbridge.repository.ExternalJobRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Fetches job postings from the Adzuna API and caches them locally.
 * Free key: https://developer.adzuna.com/  (set ADZUNA_APP_ID / ADZUNA_APP_KEY)
 */
@Service
@RequiredArgsConstructor
public class JobFetchService {

    private static final Logger log = LoggerFactory.getLogger(JobFetchService.class);
    private static final String ADZUNA_BASE = "https://api.adzuna.com/v1/api/jobs";

    private final ExternalJobRepository repository;

    @Value("${adzuna.app-id:}")
    private String appId;

    @Value("${adzuna.app-key:}")
    private String appKey;

    @Value("${adzuna.country:in}")
    private String country;

    @Value("${adzuna.results-per-page:20}")
    private int resultsPerPage;

    @Value("${adzuna.default-queries:software developer,data analyst,full stack developer,java developer}")
    private List<String> defaultQueries;

    private final RestClient restClient = RestClient.create();

    private boolean isConfigured() {
        return StringUtils.hasText(appId) && StringUtils.hasText(appKey);
    }

    /** Return cached jobs, optionally filtered by keyword/location. */
    public List<ExternalJobResponse> getJobs(String query, String location) {
        List<ExternalJob> jobs = (StringUtils.hasText(query) || StringUtils.hasText(location))
                ? repository.search(emptyToNull(query), emptyToNull(location))
                : repository.findTop100ByOrderByPostedAtDesc();
        return jobs.stream().map(ExternalJobResponse::from).collect(Collectors.toList());
    }

    /** Fetch fresh jobs from Adzuna for every configured default query. */
    public int refreshAll() {
        if (!isConfigured()) {
            log.warn("Adzuna API not configured (set adzuna.app-id / adzuna.app-key). Skipping job fetch.");
            return 0;
        }
        int saved = 0;
        for (String q : defaultQueries) {
            try {
                saved += fetchForQuery(q.trim());
            } catch (Exception e) {
                log.error("Failed to fetch Adzuna jobs for query '{}': {}", q, e.getMessage());
            }
        }
        log.info("Adzuna refresh complete: {} new/updated jobs stored.", saved);
        return saved;
    }

    /**
     * Clear all cached (non-alumni) Adzuna records and bulk-insert fresh
     * listings. Alumni/mentor-posted jobs live in a separate table and are
     * untouched. Used by the scheduled {@link JobAutomationService}.
     */
    @Transactional
    public int refreshAllReplacing() {
        if (!isConfigured()) {
            log.warn("Adzuna API not configured (set adzuna.app-id / adzuna.app-key). Skipping job refresh.");
            return 0;
        }
        Map<String, ExternalJob> fresh = new LinkedHashMap<>();
        for (String q : defaultQueries) {
            try {
                for (ExternalJob job : fetchListForQuery(q.trim())) {
                    fresh.putIfAbsent(job.getExternalId(), job); // de-dup across queries
                }
            } catch (Exception e) {
                log.error("Failed to fetch Adzuna jobs for query '{}': {}", q, e.getMessage());
            }
        }
        if (fresh.isEmpty()) {
            log.warn("Adzuna returned no listings; keeping existing cache to avoid wiping the board.");
            return 0;
        }
        repository.deleteAllInBatch();                 // clear old non-alumni records
        repository.saveAll(fresh.values());            // bulk-save fresh listings
        log.info("Adzuna automation: replaced cache with {} fresh jobs.", fresh.size());
        return fresh.size();
    }

    /** Fetch (without saving) the mapped entities for a single query. */
    private List<ExternalJob> fetchListForQuery(String query) {
        String url = UriComponentsBuilder
                .fromUriString(ADZUNA_BASE + "/" + country + "/search/1")
                .queryParam("app_id", appId)
                .queryParam("app_key", appKey)
                .queryParam("results_per_page", resultsPerPage)
                .queryParam("what", query)
                .queryParam("content-type", "application/json")
                .build()
                .toUriString();

        AdzunaSearchResponse response = restClient.get().uri(url).retrieve().body(AdzunaSearchResponse.class);
        if (response == null || response.getResults() == null) return List.of();

        List<ExternalJob> out = new ArrayList<>();
        for (AdzunaSearchResponse.AdzunaJob job : response.getResults()) {
            if (job.getId() == null) continue;
            out.add(toEntity(job));
        }
        return out;
    }

    private ExternalJob toEntity(AdzunaSearchResponse.AdzunaJob job) {
        ExternalJob entity = new ExternalJob();
        entity.setExternalId(job.getId());
        entity.setTitle(job.getTitle());
        entity.setCompany(job.getCompany() != null ? job.getCompany().getDisplayName() : null);
        entity.setLocation(job.getLocation() != null ? job.getLocation().getDisplayName() : null);
        entity.setCategory(job.getCategory() != null ? job.getCategory().getLabel() : null);
        entity.setSalaryMin(job.getSalaryMin());
        entity.setSalaryMax(job.getSalaryMax());
        entity.setContractTime(job.getContractTime());
        entity.setRedirectUrl(job.getRedirectUrl());
        entity.setDescription(job.getDescription());
        entity.setSource("Adzuna");
        entity.setPostedAt(parseDate(job.getCreated()));
        entity.setFetchedAt(LocalDateTime.now());
        return entity;
    }

    @Transactional
    protected int fetchForQuery(String query) {
        String url = UriComponentsBuilder
                .fromUriString(ADZUNA_BASE + "/" + country + "/search/1")
                .queryParam("app_id", appId)
                .queryParam("app_key", appKey)
                .queryParam("results_per_page", resultsPerPage)
                .queryParam("what", query)
                .queryParam("content-type", "application/json")
                .build()
                .toUriString();

        AdzunaSearchResponse response = restClient.get()
                .uri(url)
                .retrieve()
                .body(AdzunaSearchResponse.class);

        if (response == null || response.getResults() == null) return 0;

        int count = 0;
        for (AdzunaSearchResponse.AdzunaJob job : response.getResults()) {
            if (job.getId() == null) continue;
            if (upsert(job)) count++;
        }
        return count;
    }

    private boolean upsert(AdzunaSearchResponse.AdzunaJob job) {
        ExternalJob entity = repository.findByExternalId(job.getId()).orElseGet(ExternalJob::new);
        boolean isNew = entity.getId() == null;

        entity.setExternalId(job.getId());
        entity.setTitle(job.getTitle());
        entity.setCompany(job.getCompany() != null ? job.getCompany().getDisplayName() : null);
        entity.setLocation(job.getLocation() != null ? job.getLocation().getDisplayName() : null);
        entity.setCategory(job.getCategory() != null ? job.getCategory().getLabel() : null);
        entity.setSalaryMin(job.getSalaryMin());
        entity.setSalaryMax(job.getSalaryMax());
        entity.setContractTime(job.getContractTime());
        entity.setRedirectUrl(job.getRedirectUrl());
        entity.setDescription(job.getDescription());
        entity.setSource("Adzuna");
        entity.setPostedAt(parseDate(job.getCreated()));
        entity.setFetchedAt(LocalDateTime.now());

        repository.save(entity);
        return isNew;
    }

    private LocalDateTime parseDate(String iso) {
        if (!StringUtils.hasText(iso)) return LocalDateTime.now();
        try {
            return OffsetDateTime.parse(iso).toLocalDateTime();
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    private String emptyToNull(String s) {
        return StringUtils.hasText(s) ? s : null;
    }
}
