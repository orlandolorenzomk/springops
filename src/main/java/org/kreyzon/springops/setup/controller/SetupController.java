package org.kreyzon.springops.setup.controller;

import lombok.RequiredArgsConstructor;
import org.kreyzon.springops.common.dto.auth.AdminUserResponseDto;
import org.kreyzon.springops.common.dto.auth.UserDto;
import org.kreyzon.springops.common.dto.setup.SetupStatusDto;
import org.kreyzon.springops.setup.service.SetupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for handling setup-related operations.
 * Provides endpoints to check setup completion and initialize the first admin user.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@RestController
@RequestMapping("/setup")
@RequiredArgsConstructor
public class SetupController {

    private final SetupService setupService;

    /**
     * Endpoint to check if the setup process is complete.
     *
     * @return a {@link ResponseEntity} containing {@code true} if the setup is complete, {@code false} otherwise.
     */
    @GetMapping("/is-complete")
    public ResponseEntity<SetupStatusDto> isSetupComplete() {
        return ResponseEntity.ok(setupService.isSetupComplete());
    }

    /**
     * Endpoint to initialize the first admin user with default credentials.
     *
     * @return a {@link ResponseEntity} containing a {@link UserDto} representing the initialized admin user.
     */
    @PatchMapping("/initialize-admin")
    public ResponseEntity<AdminUserResponseDto> initializeFirstAdminUser() {
        return ResponseEntity.ok(setupService.initializeFirstAdminUser());
    }

    /**
     * Endpoint to initialize files required for the application setup.
     *
     * @param filePath the path where the files should be initialized.
     * @return a {@link ResponseEntity} containing {@code true} if the files were initialized successfully, {@code false} otherwise.
     */
    @PatchMapping("/initialize-files")
    public ResponseEntity<Boolean> initializeFiles(@RequestParam String filePath) {
        return ResponseEntity.ok(setupService.initializeFiles(filePath));
    }

    /**
     * Endpoint to initialize the secret key.
     *
     * @return a {@link ResponseEntity} indicating the result of the operation.
     */
    @PatchMapping("/initialize-secret-key")
    public ResponseEntity<Boolean> initializeSecretKey() {
        return ResponseEntity.ok(setupService.initializeSecretKey());
    }

    /**
     * Endpoint to initialize the SSH key for Git operations.
     *
     * @param sshKey the SSH key to be initialized.
     * @return a {@link ResponseEntity} indicating the result of the operation.
     */
    @PatchMapping("/initialize-git-ssh-key")
    public ResponseEntity<Boolean> initializeGitSshKey(@RequestParam String sshKey) {
        return ResponseEntity.ok(setupService.initializeGitSshKey(sshKey));
    }
}