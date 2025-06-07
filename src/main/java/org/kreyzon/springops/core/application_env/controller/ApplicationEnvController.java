package org.kreyzon.springops.core.application_env.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kreyzon.springops.common.dto.application_env.ApplicationEnvDto;
import org.kreyzon.springops.core.application_env.service.ApplicationEnvService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller class for managing application environment configurations.
 * Provides endpoints for CRUD operations on application environment variables.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@RestController
@RequestMapping("/application-env")
@RequiredArgsConstructor
@Slf4j
public class ApplicationEnvController {

    private final ApplicationEnvService applicationEnvService;

    /**
     * Retrieves environment variables for a specific application.
     *
     * @param applicationId the ID of the application
     * @return a list of ApplicationEnvDto
     */
    @GetMapping("/{applicationId}")
    public ResponseEntity<List<ApplicationEnvDto>> getApplicationEnvs(@PathVariable Integer applicationId) {
        log.info("Fetching environment variables for application ID: {}", applicationId);
        ApplicationEnvDto applicationEnvDto = applicationEnvService.findByApplicationId(applicationId);
        return ResponseEntity.ok(List.of(applicationEnvDto));
    }

    /**
     * Saves a new environment variable.
     *
     * @param applicationEnvDtoList the list of ApplicationEnvDto to save
     * @return the saved ApplicationEnvDto
     */
    @PostMapping
    public ResponseEntity<List<ApplicationEnvDto>> saveApplicationEnv(@RequestBody List<ApplicationEnvDto> applicationEnvDtoList) {
        try {
            log.info("Saving new environment variable: {}", applicationEnvDtoList);
            List<ApplicationEnvDto> savedEnv = applicationEnvService.save(applicationEnvDtoList);
            return ResponseEntity.ok(savedEnv);
        } catch (Exception e) {
            log.error("Error saving environment variable: {}", e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Deletes an environment variable by its ID.
     *
     * @param id the ID of the environment variable
     * @return the deleted ApplicationEnvDto
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApplicationEnvDto> deleteApplicationEnv(@PathVariable Integer id) {
        log.info("Deleting environment variable with ID: {}", id);
        ApplicationEnvDto deletedEnv = applicationEnvService.delete(id);
        if (deletedEnv != null) {
            return ResponseEntity.ok(deletedEnv);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}