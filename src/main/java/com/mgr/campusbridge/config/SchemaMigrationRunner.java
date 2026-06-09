package com.mgr.campusbridge.config;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Lightweight, idempotent schema fix-ups that run automatically on startup.
 *
 * Hibernate's ddl-auto=update never widens or alters existing columns, so a
 * legacy `reports` table created with a short VARCHAR or a MySQL ENUM that
 * lacks newer states (e.g. DISMISSED) causes "Data truncated for column
 * 'status'" when an admin handles a report. This runner safely expands those
 * columns to VARCHAR(32) without dropping any rows.
 *
 * Every statement is guarded so a failure here never blocks application start.
 */
@Component
@RequiredArgsConstructor
public class SchemaMigrationRunner {

    private static final Logger log = LoggerFactory.getLogger(SchemaMigrationRunner.class);

    private final JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void migrate() {
        widenColumnIfNeeded("reports", "status", "VARCHAR(32) NOT NULL DEFAULT 'OPEN'");
        widenColumnIfNeeded("reports", "target_type", "VARCHAR(32) NULL");
    }

    /**
     * Alter the column only if it isn't already a wide-enough VARCHAR. This makes
     * the migration idempotent (safe to run on every boot) and a no-op once fixed.
     */
    private void widenColumnIfNeeded(String table, String column, String targetDefinition) {
        try {
            // Skip if the table doesn't exist yet (fresh DB — Hibernate creates it correctly).
            Integer tableExists = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.tables " +
                            "WHERE table_schema = DATABASE() AND table_name = ?",
                    Integer.class, table);
            if (tableExists == null || tableExists == 0) {
                return;
            }

            List<Map<String, Object>> cols = jdbcTemplate.queryForList(
                    "SELECT DATA_TYPE, CHARACTER_MAXIMUM_LENGTH FROM information_schema.columns " +
                            "WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?",
                    table, column);
            if (cols.isEmpty()) {
                return; // column not present yet
            }

            String dataType = String.valueOf(cols.get(0).get("DATA_TYPE")).toLowerCase();
            Object maxLenObj = cols.get(0).get("CHARACTER_MAXIMUM_LENGTH");
            long maxLen = maxLenObj instanceof Number n ? n.longValue() : 0L;

            boolean alreadyOk = "varchar".equals(dataType) && maxLen >= 32;
            if (alreadyOk) {
                return; // nothing to do
            }

            jdbcTemplate.execute("ALTER TABLE " + table + " MODIFY COLUMN " + column + " " + targetDefinition);
            log.info("[SchemaMigration] Widened {}.{} -> {} (was {}{}).",
                    table, column, targetDefinition, dataType,
                    maxLen > 0 ? "(" + maxLen + ")" : "");
        } catch (Exception e) {
            // Never block startup over a best-effort migration.
            log.warn("[SchemaMigration] Skipped widening {}.{}: {}", table, column, e.getMessage());
        }
    }
}
