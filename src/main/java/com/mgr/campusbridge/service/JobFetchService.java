package com.mgr.campusbridge.service;

import com.mgr.campusbridge.dto.external.AdzunaSearchResponse;
import com.mgr.campusbridge.dto.response.ExternalJobResponse;
import com.mgr.campusbridge.entity.ExternalJob;
import com.mgr.campusbridge.repository.ExternalJobRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
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

    // --- Scheduling ---

    /** Initial fetch shortly after startup (only if nothing cached yet). */
    @EventListener(ApplicationReadyEvent.class)
    @Async
    public void onStartup() {
        if (isConfigured() && repository.count() == 0) {
            log.info("No cached jobs found — performing initial Adzuna fetch.");
            refreshAll();
        }
    }

    /** Refresh twice a day (06:00 and 18:00 server time). Configurable. */
    @Scheduled(cron = "${adzuna.refresh-cron:0 0 6,18 * * *}")
    public void scheduledRefresh() {
        refreshAll();
    }
}
