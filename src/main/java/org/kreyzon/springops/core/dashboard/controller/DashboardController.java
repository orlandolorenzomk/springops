package org.kreyzon.springops.core.dashboard.controller;

import lombok.RequiredArgsConstructor;
import org.kreyzon.springops.common.dto.dashboard.DashboardDto;
import org.kreyzon.springops.core.dashboard.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for managing the dashboard statistics.
 * This controller provides an endpoint to retrieve the number of registered applications,
 * the number of running applications, and the number of environments.
 *
 * @author Lorenzo Orlando
 */
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Retrieves the dashboard statistics including the number of registered applications,
     * running applications, and environments.
     *
     * @return ResponseEntity containing DashboardDto with the statistics.
     */
    @GetMapping
    public ResponseEntity<DashboardDto> getDashboardStats() {
        DashboardDto dashboardStats = dashboardService.getDashboardStats();
        return ResponseEntity.ok(dashboardStats);
    }
}
