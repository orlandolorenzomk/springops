package org.kreyzon.springops.setup.controller;

import lombok.RequiredArgsConstructor;
import org.kreyzon.springops.common.dto.auth.AdminUserResponseDto;
import org.kreyzon.springops.common.dto.auth.UserDto;
import org.kreyzon.springops.common.dto.setup.SetupStatusDto;
import org.kreyzon.springops.setup.service.SetupService;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
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
@CrossOrigin(origins = "*", allowedHeaders = "*")
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
     * Endpoint to initialize system information such as IP address, server name, and environment.
     *
     * @param ipAddress the IP address of the server.
     * @param serverName the name of the server.
     * @param environment the environment (e.g., development, production).
     * @return a {@link ResponseEntity} containing {@code true} if the system info was initialized successfully, {@code false} otherwise.
     */
    @PatchMapping("/initialize-system-info")
    public ResponseEntity<Boolean> initializeSystemInfo(
            @RequestParam String ipAddress,
            @RequestParam String serverName,
            @RequestParam String environment
    ) {
        return ResponseEntity.ok(setupService.initializeSystemInfo(ipAddress, serverName, environment));
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
}