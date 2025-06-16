package org.kreyzon.springops.core.dashboard.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kreyzon.springops.common.dto.dashboard.DashboardDto;
import org.kreyzon.springops.core.application.service.ApplicationService;
import org.kreyzon.springops.core.deployment.service.DeploymentService;
import org.springframework.stereotype.Service;

/**
 * Service for managing the dashboard statistics.
 * This service provides methods to retrieve the number of registered applications,
 * the number of running applications, and the number of environments.
 *
 * @author Lorenzo Orlando
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final ApplicationService applicationService;

    private final DeploymentService deploymentService;

    /**
     * Retrieves the dashboard statistics including the number of registered applications,
     * running applications, and environments.
     *
     * @return DashboardDto containing the statistics.
     */
    public DashboardDto getDashboardStats() {
        int registeredApps = applicationService.findAll().size();
        int runningApps = deploymentService.findActiveRunningDeployments().size();
        int environments = 1; // We only handle one environment for now

        return new DashboardDto(registeredApps, runningApps, environments);
    }
}
