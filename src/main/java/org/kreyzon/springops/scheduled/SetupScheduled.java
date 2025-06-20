package org.kreyzon.springops.scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kreyzon.springops.core.os_info.service.OsInfoService;
import org.kreyzon.springops.setup.domain.Setup;
import org.kreyzon.springops.setup.service.SetupService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * Scheduled task to populate OS information into the Setup entity.
 * This task runs every 10 minutes.
 * It retrieves OS information and updates the Setup entity with the IP address and server name.
 * If the Setup entity is not found, it logs a warning and skips the update.
 *
 * @author Lorenzo Orlando
 *
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SetupScheduled {

    private final OsInfoService osInfoService;

    private final SetupService setupService;

    /**
     * Scheduled task to populate OS information into the Setup entity.
     * This task runs every 10 minutes.
     *
     * @throws IOException If an error occurs while retrieving OS information.
     */
    @Scheduled(fixedRate = 600000) // 10 minutes
    public void populateOsInfo() throws IOException {
        Setup setup = setupService.getSetup();
        if (setup == null) {
            log.warn("Setup not found, skipping.");
            return;
        }

        log.info("Populating OS info...");
        Map<String, String> osInfoMap = osInfoService.getOsInfo();

        setupService.initializeSystemInfo(
                osInfoMap.getOrDefault("ipAddress", "Unknown"),
                osInfoMap.getOrDefault("hostname", "Unknown")
        );

        log.info("OS info populated successfully.");
    }
}
