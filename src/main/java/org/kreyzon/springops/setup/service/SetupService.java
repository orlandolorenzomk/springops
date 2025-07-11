package org.kreyzon.springops.setup.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kreyzon.springops.auth.service.UserService;
import org.kreyzon.springops.common.dto.auth.AdminUserResponseDto;
import org.kreyzon.springops.common.dto.auth.UserDto;
import org.kreyzon.springops.common.dto.setup.SetupStatusDto;
import org.kreyzon.springops.common.exception.SpringOpsException;
import org.kreyzon.springops.common.utils.*;
import org.kreyzon.springops.config.ApplicationConfig;
import org.kreyzon.springops.setup.domain.Setup;
import org.kreyzon.springops.setup.repository.SetupRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
     * @throws SpringOpsException with {@link HttpStatus#NOT_FOUND} if the setup entity is not found.
     * @return a {@link SetupStatusDto} containing the setup status and pending initializations.
     */
    public SetupStatusDto isSetupComplete() {
        Setup setup = setupRepository.findSetup();
        if (setup == null) {
            log.error("Setup entity not found. Cannot proceed with the operation.");
            throw new SpringOpsException("Setup entity not found.", HttpStatus.NOT_FOUND);
        }

        Boolean setupComplete = checkSetupCompleteStatus(setup);

        return SetupStatusDto.builder()
                .isSetupComplete(setupComplete)
                .isFirstAdminInitialized(setup.getIsFirstAdminInitialized())
                .isFilesRootInitialized(setup.getIsFilesRootInitialized())
                .ipAddress(setup.getIpAddress())
                .serverName(setup.getServerName())
                .environment(setup.getEnvironment())
                .build();
    }

    /**
     * Initializes the system information such as IP address, server name, and environment.
     * Saves the provided information to the setup entity in the database.
     *
     * @param ipAddress   the IP address of the server.
     * @param serverName  the name of the server.
     * @param environment the environment (e.g., development, production).
     * @return {@code true} if the initialization was successful, {@code false} otherwise.
     */
    public Boolean initializeSystemInfo(String ipAddress, String serverName, String environment) {
        log.info("Initializing system information with IP: {}, Server Name: {}, Environment: {}", ipAddress, serverName, environment);

        Setup setup = getSetup();
        setup.setIpAddress(ipAddress);
        setup.setServerName(serverName);
        setup.setEnvironment(environment);
        setupRepository.save(setup);

        log.info("System information initialized successfully.");
        return true;
    }

    /**
     * Initializes the system information with IP address and server name.
     * This method is a simplified version that does not include the environment.
     *
     * @param ipAddress  the IP address of the server.
     * @param serverName the name of the server.
     */
    public void initializeSystemInfo(String ipAddress, String serverName) {
        log.info("Initializing system information with IP: {}, Server Name: {}", ipAddress, serverName);

        Setup setup = getSetup();
        setup.setIpAddress(ipAddress);
        setup.setServerName(serverName);
        setupRepository.save(setup);

        log.info("System information initialized successfully.");
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
     * @throws SpringOpsException with {@link HttpStatus#CONFLICT} if the setup is already complete or the admin user already exists.
     * @return a {@link UserDto} representing the initialized admin user.
     */
    public AdminUserResponseDto initializeFirstAdminUser() {
        log.info("Initializing first admin user");

        if (isSetupComplete().getIsFirstAdminInitialized()) {
            log.warn("Setup is already complete. Skipping admin user initialization.");
            throw new SpringOpsException("Setup is already complete. Cannot initialize admin user.", HttpStatus.CONFLICT);
        }

        if (userService.findAll().stream()
                .anyMatch(user -> user.getUsername().equals(applicationConfig.getStandardAdminUsername()))) {
            log.warn("First admin user already exists. Skipping initialization.");
            throw new SpringOpsException("First admin user already exists. Cannot initialize again.", HttpStatus.CONFLICT);
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
     * @throws SpringOpsException with {@link HttpStatus#NOT_FOUND} if the setup entity is not found or the directory creation fails.
     * @throws SpringOpsException with {@link HttpStatus#CONFLICT} if the files root is already initialized.
     * @throws SpringOpsException with {@link HttpStatus#INTERNAL_SERVER_ERROR} if the applications root or subdirectory creation fails.
     */
    public Boolean initializeFiles(String filePath) {
        log.info("Initializing files with root directory: {}", filePath);

        if (filePath == null || filePath.isEmpty()) {
            log.error("File path is null or empty.");
            throw new SpringOpsException("File path cannot be null or empty.", HttpStatus.BAD_REQUEST);
        }

        if (isSetupComplete().getIsFilesRootInitialized()) {
            log.warn("Files root is already initialized. Skipping initialization.");
            throw new SpringOpsException("Files root is already initialized. Cannot initialize again.", HttpStatus.CONFLICT);
        }

        Setup setup = getSetup();

        String rootDirectoryPath = filePath.concat(applicationConfig.getRootDirectoryName());
        boolean isRootDirectoryCreated = FileUtils.createDirectory(rootDirectoryPath);
        if (!isRootDirectoryCreated) {
            log.error("Failed to create root directory: {}", rootDirectoryPath);
            throw new SpringOpsException("Failed to create root directory.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Path applicationsSubdirectoryPath = Path.of(rootDirectoryPath, applicationConfig.getDirectoryApplications());
        try {
            Files.createDirectories(applicationsSubdirectoryPath);
            log.info("Applications subdirectory created successfully: {}", applicationsSubdirectoryPath);
        } catch (IOException e) {
            log.error("Failed to create applications subdirectory: {}", applicationsSubdirectoryPath, e);
            throw new SpringOpsException("Failed to create applications subdirectory.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Path applicationLogsSubdirectoryPath = Path.of(applicationsSubdirectoryPath.toString(), applicationConfig.getDirectoryApplicationLogs());
        try {
            Files.createDirectories(applicationLogsSubdirectoryPath);
            log.info("Application logs subdirectory created successfully: {}", applicationLogsSubdirectoryPath);
        } catch (IOException e) {
            log.error("Failed to create application logs subdirectory: {}", applicationLogsSubdirectoryPath, e);
            throw new SpringOpsException("Failed to create application logs subdirectory.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Path systemVersionsSubdirectoryPath = Path.of(rootDirectoryPath, applicationConfig.getDirectorySystemVersions());
        try {
            Files.createDirectories(systemVersionsSubdirectoryPath);
            log.info("System versions subdirectory created successfully: {}", systemVersionsSubdirectoryPath);
        } catch (IOException e) {
            log.error("Failed to create system versions subdirectory: {}", systemVersionsSubdirectoryPath, e);
            throw new SpringOpsException("Failed to create system versions subdirectory.", HttpStatus.INTERNAL_SERVER_ERROR);
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
     * @throws SpringOpsException with {@link HttpStatus#NOT_FOUND} if the setup entity is not found.
     */
    public Setup getSetup() {
        Setup setup = setupRepository.findSetup();
        if (setup == null) {
            log.error("Setup entity not found. Cannot proceed with the operation.");
            throw new SpringOpsException("Setup entity not found.", HttpStatus.NOT_FOUND);
        }
        return setup;
    }
}