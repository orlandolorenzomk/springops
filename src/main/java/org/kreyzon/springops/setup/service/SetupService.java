package org.kreyzon.springops.setup.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kreyzon.springops.auth.service.UserService;
import org.kreyzon.springops.common.dto.auth.AdminUserResponseDto;
import org.kreyzon.springops.common.dto.auth.UserDto;
import org.kreyzon.springops.common.dto.setup.SetupStatusDto;
import org.kreyzon.springops.common.utils.*;
import org.kreyzon.springops.config.ApplicationConfig;
import org.kreyzon.springops.setup.domain.Setup;
import org.kreyzon.springops.setup.repository.SetupRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Service class for handling setup-related operations.
 * Provides methods to check setup completion and initialize the first admin user.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SetupService {

    private final SetupRepository setupRepository;

    private final UserService userService;

    private final ApplicationConfig applicationConfig;

    /**
     * Checks the setup process status and returns a DTO indicating
     * whether the setup is complete and which initializations are pending.
     *
     * @return a {@link SetupStatusDto} containing the setup status and pending initializations.
     */
    public SetupStatusDto isSetupComplete() {
        Setup setup = setupRepository.findSetup();
        if (setup == null) {
            log.error("Setup entity not found. Cannot proceed with the operation.");
            throw new IllegalStateException("Setup entity not found.");
        }

        Boolean setupComplete = checkSetupCompleteStatus(setup);

        return SetupStatusDto.builder()
                .isSetupComplete(setupComplete)
                .isFirstAdminInitialized(setup.getIsFirstAdminInitialized())
                .isFilesRootInitialized(setup.getIsFilesRootInitialized())
                .build();
    }

    /**
     * Checks if the setup is complete by verifying the status of various initializations.
     * Returns {@code true} if all required initializations are complete, {@code false} otherwise.
     *
     * @param setup the {@link Setup} entity containing the initialization statuses.
     * @return {@code true} if the setup is complete, {@code false} otherwise.
     */
    private Boolean checkSetupCompleteStatus(Setup setup) {
        boolean isFirstAdminInitialized = setup.getIsFirstAdminInitialized() != null && setup.getIsFirstAdminInitialized();
        boolean isFilesRootInitialized = setup.getIsFilesRootInitialized() != null && setup.getIsFilesRootInitialized();

        return isFirstAdminInitialized && isFilesRootInitialized;
    }

    /**
     * Initializes the first admin user with default credentials.
     * Generates a random password for the admin user and saves it.
     *
     * @return a {@link UserDto} representing the initialized admin user.
     */
    public AdminUserResponseDto initializeFirstAdminUser() {
        log.info("Initializing first admin user");

        if (isSetupComplete().getIsFirstAdminInitialized()) {
            log.warn("Setup is already complete. Skipping admin user initialization.");
            throw new IllegalStateException("Setup is already complete. Cannot initialize admin user.");
        }

        if (userService.findAll().stream()
                .anyMatch(user -> user.getUsername().equals(applicationConfig.getStandardAdminUsername()))) {
            log.warn("First admin user already exists. Skipping initialization.");
            throw new IllegalStateException("First admin user already exists. Cannot initialize again.");
        }

        String password = PasswordGenerator.generateRandomPassword(applicationConfig.getStandardAdminPasswordLength());
        UserDto firstAdminUser = UserDto
                .builder()
                .username(applicationConfig.getStandardAdminUsername())
                .email(applicationConfig.getStandardAdminEmail())
                .password(password)
                .build();
        firstAdminUser = userService.create(firstAdminUser);
        log.info("First admin user initialized: {}", firstAdminUser.getUsername());

        Setup setup = getSetup();
        setup.setIsFirstAdminInitialized(true);
        setupRepository.save(setup);

        return AdminUserResponseDto.builder()
                .user(firstAdminUser)
                .generatedPassword(password)
                .build();
    }

    /**
     * Initializes the files required for the application setup.
     * Saves the provided file path to the setup entity and creates the root directory.
     *
     * @param filePath the file path to be saved and used as the root directory.
     * @return {@code true} if the files were initialized successfully, {@code false} otherwise.
     * @throws IllegalStateException if the setup entity is not found or the directory creation fails.
     */
    public Boolean initializeFiles(String filePath) {
        log.info("Initializing files with root directory: {}", filePath);

        if (isSetupComplete().getIsFilesRootInitialized()) {
            log.warn("Files root is already initialized. Skipping initialization.");
            throw new IllegalStateException("Files root is already initialized. Cannot initialize again.");
        }

        Setup setup = getSetup();

        String rootDirectoryPath = filePath.concat(Constants.ROOT_DIRECTORY_NAME);
        boolean isRootDirectoryCreated = FileUtils.createDirectory(rootDirectoryPath);
        if (!isRootDirectoryCreated) {
            log.error("Failed to create root directory: {}", rootDirectoryPath);
            throw new IllegalStateException("Failed to create root directory.");
        }

        Path applicationsSubdirectoryPath = Path.of(rootDirectoryPath, Constants.DIRECTORY_APPLICATIONS);
        try {
            Files.createDirectories(applicationsSubdirectoryPath);
            log.info("Applications subdirectory created successfully: {}", applicationsSubdirectoryPath);
        } catch (IOException e) {
            log.error("Failed to create applications subdirectory: {}", applicationsSubdirectoryPath, e);
            throw new IllegalStateException("Failed to create applications subdirectory.", e);
        }

        setup.setFilesRoot(filePath);
        setup.setIsFilesRootInitialized(true);
        setupRepository.save(setup);
        log.info("File path saved to setup entity: {}", filePath);

        log.info("Root directory and applications subdirectory created successfully.");
        return true;
    }

    /**
     * Retrieves the setup entity from the database.
     * Ensures that the setup entity exists before proceeding with any operation.
     *
     * @return the {@link Setup} entity.
     * @throws IllegalStateException if the setup entity is not found in the database.
     */
    public Setup getSetup() {
        Setup setup = setupRepository.findSetup();
        if (setup == null) {
            log.error("Setup entity not found. Cannot proceed with the operation.");
            throw new IllegalStateException("Setup entity not found.");
        }
        return setup;
    }
}