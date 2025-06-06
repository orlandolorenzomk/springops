package org.kreyzon.springops.setup.initializer;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kreyzon.springops.common.utils.DatabaseUtils;
import org.kreyzon.springops.setup.domain.Setup;
import org.kreyzon.springops.setup.repository.SetupRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Initializes the SpringOps platform by creating a default setup row
 * in the database if none exists.
 * <p>
 * This component runs once at application startup.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StartupInitializer {

    private final SetupRepository setupRepository;

    /**
     * Checks if the setup table is empty and inserts a default row
     * if no setup has been completed yet.
     */
    @PostConstruct
    public void init() {
        log.info("Checking if setup row exists in the database...");
        if (DatabaseUtils.isTableEmpty(setupRepository.count())) {
            Setup setup = new Setup(
                    UUID.randomUUID(),
                    null,
                    false,
                    Instant.now(),
                    null,
                    false,
                    false
            );
            setupRepository.save(setup);
            log.info("Initialized SpringOps setup row. Please complete the setup process by visiting the application.");
        } else {
            log.info("Setup row already exists. Skipping initialization.");
        }
    }
}
