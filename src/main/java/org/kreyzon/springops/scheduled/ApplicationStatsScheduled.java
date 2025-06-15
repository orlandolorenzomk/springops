package org.kreyzon.springops.scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kreyzon.springops.common.dto.application_stats.ApplicationStatsDto;
import org.kreyzon.springops.common.dto.deployment.DeploymentDto;
import org.kreyzon.springops.common.exception.SpringOpsException;
import org.kreyzon.springops.common.utils.PidUtils;
import org.kreyzon.springops.core.application.entity.Application;
import org.kreyzon.springops.core.application.service.ApplicationLookupService;
import org.kreyzon.springops.core.application_stats.service.ApplicationStatsService;
import org.kreyzon.springops.core.deployment.service.DeploymentService;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Scheduled task to update application statistics.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ApplicationStatsScheduled {

    private final DeploymentService deploymentService;

    private final ApplicationStatsService applicationStatsService;

    private final ApplicationLookupService applicationLookupService;

    /**
     * Scheduled task that runs every 2 minutes to update application statistics.
     * It retrieves active deployments, fetches their CPU and memory usage,
     * and saves the statistics in the database.
     */
    @Scheduled(fixedRate = 120000)
    public void updateApplicationStatus() {
        log.info("Updating application status...");

        List<DeploymentDto> deployments = deploymentService.findActiveRunningDeployments();
        if (deployments.isEmpty()) {
            log.info("No active deployments found.");
            return;
        }

        log.info("Found {} active deployments: {}", deployments.size(), deployments);

        deployments.forEach(deployment -> {
            try {
                Application application = applicationLookupService.findEntityById(deployment.getApplicationId());
                ApplicationStatsDto statsDto = PidUtils.getCpuMemUsage(deployment.getPid());
                applicationStatsService.save(statsDto, deployment.getPid(), deployment.getApplicationId(), OffsetDateTime.now().toString());

            } catch (Exception e) {
                throw new SpringOpsException("Failed to retrieve CPU and memory usage for deployment with PID: " + deployment.getPid(), HttpStatus.INTERNAL_SERVER_ERROR);
            }

            log.debug("Successfully updated application stats for deployment with PID: {}", deployment.getPid());
        });
    }

    /**
     * Scheduled task that runs every hour to delete old application statistics.
     * It removes statistics older than 1 day from the database.
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void deleteOldStats() {
        int days = 1;
        log.info("Deleting old application stats older than {} day(s)", days);
        boolean success = applicationStatsService.deleteStatsOlderThanDays(days);
        if (success) {
            log.info("Successfully deleted old application stats older than {} day(s)", days);
        } else {
            log.warn("No old application stats found to delete older than {} day(s)", days);
        }
    }
}
