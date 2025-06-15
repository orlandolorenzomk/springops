package org.kreyzon.springops.core.application_stats.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kreyzon.springops.common.dto.application_stats.ApplicationStatsDto;
import org.kreyzon.springops.core.application_stats.service.ApplicationStatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for managing application statistics.
 * Provides endpoints for retrieving application statistics over a time period.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@RestController
@RequestMapping("/application-stats")
@RequiredArgsConstructor
@Slf4j
public class ApplicationStatsController {

    private final ApplicationStatsService applicationStatsService;

    /**
     * Retrieves application statistics for a given application ID over a time period.
     *
     * @param applicationId the ID of the application
     * @param startTimestamp the start of the time period
     * @param endTimestamp the end of the time period
     * @return a list of ApplicationStatsDto within the time period
     */
    @GetMapping
    public List<ApplicationStatsDto> getStatsOverTimePeriod(
            @RequestParam Integer applicationId,
            @RequestParam String startTimestamp,
            @RequestParam String endTimestamp
    ) {
        return applicationStatsService.getStatsOverTimePeriod(applicationId, startTimestamp, endTimestamp);
    }
}