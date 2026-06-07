package com.mgr.campusbridge.service;

import com.mgr.campusbridge.repository.ExternalJobRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Background worker that keeps the cached Adzuna job board fresh without making
 * students wait on external API calls during page loads.
 *
 * Runs every 6 hours: clears old non-alumni job records and bulk-saves fresh
 * listings. Alumni/mentor-posted jobs live in a separate table and are untouched.
 */
@Service
@RequiredArgsConstructor
public class JobAutomationService {

    private static final Logger log = LoggerFactory.getLogger(JobAutomationService.class);

    private final JobFetchService jobFetchService;
    private final ExternalJobRepository externalJobRepository;

    /** Every 6 hours, on the hour (00:00, 06:00, 12:00, 18:00). */
    @Scheduled(cron = "0 0 */6 * * *")
    public void scheduledJobRefresh() {
        log.info("[JobAutomation] Scheduled Adzuna refresh starting…");
        try {
            int count = jobFetchService.refreshAllReplacing();
            log.info("[JobAutomation] Scheduled refresh complete: {} jobs cached.", count);
        } catch (Exception e) {
            log.error("[JobAutomation] Scheduled refresh failed: {}", e.getMessage());
        }
    }

    /** Seed the cache once on startup if it's empty so the board isn't blank. */
    @EventListener(ApplicationReadyEvent.class)
    @Async
    public void seedOnStartup() {
        if (externalJobRepository.count() == 0) {
            log.info("[JobAutomation] Empty job cache on startup — seeding from Adzuna.");
            try {
                jobFetchService.refreshAllReplacing();
            } catch (Exception e) {
                log.error("[JobAutomation] Startup seed failed: {}", e.getMessage());
            }
        }
    }
}
