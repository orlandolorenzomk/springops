package org.kreyzon.springops.core.os_info.controller;

import lombok.RequiredArgsConstructor;
import org.kreyzon.springops.core.os_info.service.OsInfoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller for handling OS information requests.
 * Provides endpoints to retrieve system information collected from the host machine.
 *
 * @author Lorenzo Orlando
 */
@RestController
@RequestMapping("/os-info")
@RequiredArgsConstructor
public class OsInfoController {

    private final OsInfoService osInfoService;

    /**
     * Retrieves comprehensive operating system and hardware information.
     *
     * @return ResponseEntity containing a map of system properties with their corresponding values.
     *         Returns HTTP 200 (OK) with the system information on success.
     *         Returns HTTP 500 (Internal Server Error) if the information cannot be retrieved.
     *
     * @apiNote This endpoint executes a shell script to gather system information including:
     *          - Hostname
     *          - OS details
     *          - CPU information
     *          - Memory usage
     *          - Disk space
     *          - Network configuration
     *          - Running processes
     *          - And other system metrics
     *
     * @example Example response:
     * {
     *     "hostname": "server1",
     *     "operatingSystem": "Ubuntu 20.04.3 LTS",
     *     "cpuModel": "Intel(R) Xeon(R) CPU E5-2678 v3 @ 2.50GHz",
     *     "memoryTotal": "15.6G",
     *     "memoryUsed": "4.2G"
     * }
     */
    @GetMapping
    public ResponseEntity<Map<String, String>> getOsInfo() {
        try {
            Map<String, String> osInfo = osInfoService.getOsInfo();
            return ResponseEntity.ok(osInfo);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}