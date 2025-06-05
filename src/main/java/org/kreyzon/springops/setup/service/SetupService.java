package org.kreyzon.springops.setup.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kreyzon.springops.auth.service.UserService;
import org.kreyzon.springops.common.dto.auth.AdminUserResponseDto;
import org.kreyzon.springops.common.dto.auth.UserDto;
import org.kreyzon.springops.common.dto.setup.SetupStatusDto;
import org.kreyzon.springops.common.utils.*;
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

    /**
     * Checks the setup process status and returns a DTO indicating
     * whether the setup is complete and which initializations are pending.
     *
     * @return a {@link SetupStatusDto} containing the setup status and pending initializations.
     */
    public SetupStatusDto isSetupComplete() {
        log.info("Checking setup status");

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
                .isSecretKeyInitialized(setup.getIsSecretKeyInitialized())
                .isGitSshKeyInitialized(setup.getIsGitSshKeyInitialized())
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
        boolean isSecretKeyInitialized = setup.getIsSecretKeyInitialized() != null && setup.getIsSecretKeyInitialized();
        boolean isGitSshKeyInitialized = setup.getIsGitSshKeyInitialized() != null && setup.getIsGitSshKeyInitialized();

        return isFirstAdminInitialized && isFilesRootInitialized && isSecretKeyInitialized && isGitSshKeyInitialized;
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
                .anyMatch(user -> user.getUsername().equals(Constants.STANDARD_ADMIN_USERNAME))) {
            log.warn("First admin user already exists. Skipping initialization.");
            throw new IllegalStateException("First admin user already exists. Cannot initialize again.");
        }

        String password = PasswordGenerator.generateRandomPassword(Constants.STANDARD_ADMIN_PASSWORD_LENGTH);
        UserDto firstAdminUser = UserDto
                .builder()
                .username(Constants.STANDARD_ADMIN_USERNAME)
                .email(Constants.STANDARD_ADMIN_EMAIL)
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

        boolean isDirectoryCreated = FileUtils.createDirectory(filePath.concat(Constants.ROOT_DIRECTORY_NAME));
        if (!isDirectoryCreated) {
            log.error("Failed to create root directory: {}", filePath);
            throw new IllegalStateException("Failed to create root directory.");
        }

        setup.setFilesRoot(filePath);
        setup.setIsFilesRootInitialized(true);
        setupRepository.save(setup);
        log.info("File path saved to setup entity: {}", filePath);

        log.info("Root directory created successfully: {}", filePath);
        return true;
    }

    /**
     * Initializes the secret key and stores it in a file within the setup's files root directory.
     * Ensures that the files root is initialized and the secret key is not already initialized.
     * Generates a random secret key, writes it to a file, and updates the setup entity.
     *
     * @return {@code true} if the secret key is successfully initialized.
     * @throws IllegalStateException if the files root is not initialized, the secret key is already initialized,
     *                               or if writing the secret key to the file fails.
     */
    public Boolean initializeSecretKey() {
        Setup setup = getSetup();
        String filesRoot = setup.getFilesRoot();
        if (filesRoot == null || filesRoot.isBlank()) {
            log.error("Files root is not initialized. Cannot create secret key file.");
            throw new IllegalStateException("Files root is not initialized.");
        }

        String filePath = filesRoot + "/" + Constants.SECRET_KEY_FILE_PATH;
        log.info("Initializing secret key and storing it in file: {}", filePath);

        if (isSetupComplete().getIsSecretKeyInitialized()) {
            log.warn("Secret key is already initialized. Skipping initialization.");
            throw new IllegalStateException("Secret key is already initialized. Cannot initialize again.");
        }

        String secretKey = SecretKeyGenerator.generateAesSecretKey();
        log.info("[TRUNCATED] Generated secret key: {}", secretKey.substring(4, 7));

        try {
            Path path = Path.of(filePath);
            Files.createDirectories(path.getParent());
            Files.writeString(path, secretKey, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("Secret key successfully written to file: {}", filePath);
        } catch (IOException e) {
            log.error("Failed to write secret key to file: {}", filePath, e);
            throw new IllegalStateException("Failed to write secret key to file.", e);
        }

        setup.setIsSecretKeyInitialized(true);
        setupRepository.save(setup);
        log.info("Secret key initialization status updated in the database.");

        return true;
    }

    /**
     * Initializes the SSH key by encrypting it with the secret key.
     * Updates the setup entity with the encrypted SSH key.
     *
     * @param sshKey the SSH key to encrypt and store.
     * @return {@code true} if the SSH key is successfully initialized.
     * @throws IllegalStateException if the secret key file is missing or encryption fails.
     */
    public Boolean initializeGitSshKey(String sshKey) {
        Setup setup = getSetup();

        String filesRoot = setup.getFilesRoot();
        if (filesRoot == null || filesRoot.isBlank()) {
            log.error("Files root is not initialized. Cannot initialize SSH key.");
            throw new IllegalStateException("Files root is not initialized.");
        }

        String secretKeyFilePath = Path.of(filesRoot, Constants.SECRET_KEY_FILE_PATH).toString();
        String sshKeyFilePath = Path.of(filesRoot, Constants.SSH_KEY_FILE_PATH).toString();
        try {
            String secretKey = EncryptionUtils.readSecretKey(secretKeyFilePath);

            String encryptedSshKey = EncryptionUtils.encrypt(sshKey, secretKey);

            Path path = Path.of(sshKeyFilePath);
            Files.createDirectories(path.getParent());
            Files.writeString(path, encryptedSshKey, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("Encrypted SSH key successfully written to file: {}", sshKeyFilePath);

            setup.setIsGitSshKeyInitialized(true);
            setupRepository.save(setup);

            log.info("SSH key initialization status updated in the database.");
            return true;
        } catch (Exception e) {
            log.error("Failed to initialize SSH key: {}", e.getMessage(), e);
            throw new IllegalStateException("Failed to initialize SSH key.", e);
        }
    }

    /**
     * Retrieves the setup entity from the database.
     * Ensures that the setup entity exists before proceeding with any operation.
     *
     * @return the {@link Setup} entity.
     * @throws IllegalStateException if the setup entity is not found in the database.
     */
    private Setup getSetup() {
        Setup setup = setupRepository.findSetup();
        if (setup == null) {
            log.error("Setup entity not found. Cannot proceed with the operation.");
            throw new IllegalStateException("Setup entity not found.");
        }
        return setup;
    }
}