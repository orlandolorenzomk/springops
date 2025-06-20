package org.kreyzon.springops.scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kreyzon.springops.audits.service.AuditService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Scheduled service for managing audit records.
 * This service contains a scheduled task that deletes audit records
 * older than one month from the database.
 *
 * @author Lorenzo Orlando
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditScheduled {

    private final AuditService auditService;

    /**
     * Scheduled task to delete audits older than one month.
     * This method runs daily at midnight and removes audit records
     * that are older than one month from the database.
     */
    @Scheduled(cron = "0 0 0 * * ?") // Daily at midnight
    public void deleteAuditsOlderThanAMonth() {
        log.info("Starting scheduled task to delete audits older than one month");

        try {
            Integer months = 12;
            auditService.deleteAuditsOlderThanNMonths(12);
            log.info("Successfully deleted audits older than one month");
        } catch (Exception e) {
            log.error("Error occurred while deleting audits older than one month", e);
        }
    }
}
