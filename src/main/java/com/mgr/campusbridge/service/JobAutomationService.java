package com.mgr.campusbridge.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Background worker that keeps the cached Adzuna job board fresh without making
 * students wait on external API calls during page loads.
 *
 * Uses fixedRate so the very first refresh runs immediately on application
 * startup, then repeats every 6 hours from that point — guaranteeing the
 * database is populated for live demos. Each run clears old non-alumni job
 * records and bulk-saves fresh listings. Alumni/mentor-posted jobs live in a
 * separate table and are untouched.
 */
@Service
@RequiredArgsConstructor
public class JobAutomationService {

    private static final Logger log = LoggerFactory.getLogger(JobAutomationService.class);

    /** 6 hours in milliseconds. */
    private static final long SIX_HOURS_MS = 6L * 60 * 60 * 1000;

    private final JobFetchService jobFetchService;

    /**
     * Runs immediately at startup (no initial delay) and then every 6 hours.
     * The interval is also overridable via the "adzuna.refresh-rate-ms" property.
     */
    @Scheduled(fixedRateString = "${adzuna.refresh-rate-ms:" + SIX_HOURS_MS + "}", initialDelay = 0)
    public void scheduledJobRefresh() {
        log.info("[JobAutomation] Adzuna refresh starting…");
        try {
            int count = jobFetchService.refreshAllReplacing();
            log.info("[JobAutomation] Refresh complete: {} jobs cached. Next run in ~6 hours.", count);
        } catch (Exception e) {
            log.error("[JobAutomation] Refresh failed: {}", e.getMessage());
        }
    }
}
