package org.kreyzon.springops.core.application_stats.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kreyzon.springops.common.dto.application_stats.ApplicationStatsDto;
import org.kreyzon.springops.core.application.entity.Application;
import org.kreyzon.springops.core.application.service.ApplicationLookupService;
import org.kreyzon.springops.core.application_stats.entity.ApplicationStats;
import org.kreyzon.springops.core.application_stats.repository.ApplicationStatsRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing application statistics.
 * This class is responsible for handling business logic related to application statistics,
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationStatsService {

    private final ApplicationStatsRepository applicationStatsRepository;

    private final ApplicationLookupService applicationLookupService;

    /**
     * Saves the application statistics for a given process ID and application ID.
     *
     * @param statsDto the DTO containing the statistics to save
     * @param pid the process ID
     * @param applicationId the ID of the application
     * @param timestamp the timestamp of the statistics
     * @return the saved ApplicationStatsDto
     */
    public ApplicationStatsDto save(ApplicationStatsDto statsDto, Integer pid, Integer applicationId, String timestamp) {
        log.debug("Saving application stats for PID {} of application {} at {}", pid, applicationId, timestamp);

        Application application = applicationLookupService.findEntityById(applicationId);

        ApplicationStats statsEntity = statsDto.toEntity(
                application,
                pid,
                java.time.OffsetDateTime.parse(timestamp)
        );

        ApplicationStats savedStats = applicationStatsRepository.save(statsEntity);

        return ApplicationStatsDto.fromEntity(savedStats);
    }

    /**
     * Retrieves application statistics for a given application ID over a time period.
     *
     * @param applicationId the ID of the application
     * @param startTimestamp the start of the time period
     * @param endTimestamp the end of the time period
     * @return a list of ApplicationStatsDto within the time period
     */
    public List<ApplicationStatsDto> getStatsOverTimePeriod(Integer applicationId, String startTimestamp, String endTimestamp) {
        log.info("Fetching application stats for application {} between {} and {}", applicationId, startTimestamp, endTimestamp);

        OffsetDateTime start = OffsetDateTime.parse(startTimestamp);
        OffsetDateTime end = OffsetDateTime.parse(endTimestamp);

        List<ApplicationStats> statsList = applicationStatsRepository.findByApplicationIdAndTimestampBetween(applicationId, start, end);

        return statsList.stream()
                .map(ApplicationStatsDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Deletes application statistics older than a specified number of days.
     *
     * @param days the number of days to keep; stats older than this will be deleted
     * @return true if any records were deleted, false otherwise
     */
    public boolean deleteStatsOlderThanDays(int days) {
        log.debug("Deleting application stats older than {} days", days);

        OffsetDateTime cutoffDate = OffsetDateTime.now().minusDays(days);
        List<ApplicationStats> oldStats = applicationStatsRepository.findByTimestampBefore(cutoffDate);

        if (oldStats.isEmpty()) {
            log.info("No old stats found to delete.");
            return false;
        }

        applicationStatsRepository.deleteAll(oldStats);
        log.info("Deleted {} old application stats records.", oldStats.size());
        return true;
    }
}
