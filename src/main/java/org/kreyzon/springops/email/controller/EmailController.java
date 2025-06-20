package org.kreyzon.springops.email.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kreyzon.springops.email.dto.EmailConfigurationDto;
import org.kreyzon.springops.email.request.EmailConfigurationRequest;
import org.kreyzon.springops.email.service.EmailServiceBridge;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for managing email configurations.
 *
 * @author Domenico Ferraro
 */
@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", allowedHeaders = "*") // Uncomment if CORS is needed
public class EmailController {

    private final EmailServiceBridge emailServiceBridge;

    /**
     * Saves a new email configuration.
     *
     * @param emailConfigurationRequest the email configuration request
     * @return the saved email configuration
     */
    @PostMapping("/save")
    public ResponseEntity<EmailConfigurationDto> save(@Valid @NotNull @RequestBody EmailConfigurationRequest emailConfigurationRequest) {
        log.info("Saving email configuration: {}", emailConfigurationRequest);
        EmailConfigurationDto savedConfiguration = emailServiceBridge.save(emailConfigurationRequest);
        return ResponseEntity.ok(savedConfiguration);
    }

    /**
     * Retrieves an email configuration by its identifier.
     *
     * @param id the identifier of the configuration
     * @return the email configuration
     */
    @GetMapping("/{id}")
    public ResponseEntity<EmailConfigurationDto> getById(@NotBlank @PathVariable("id") String id) {
        log.info("Retrieving email configuration with ID: {}", id);
        EmailConfigurationDto emailConfiguration = emailServiceBridge.findById(UUID.fromString(id));
        return ResponseEntity.ok(emailConfiguration);
    }

    /**
     * Deletes an email configuration by its identifier.
     *
     * @param id the identifier of the configuration to delete
     * @return a response indicating the deletion status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@NotBlank @PathVariable("id") String id) {
        log.info("Deleting email configuration with ID: {}", id);
        emailServiceBridge.deleteById(UUID.fromString(id));
        return ResponseEntity.ok().build();
    }

    /**
     * Updates an existing email configuration.
     *
     * @param emailConfigurationRequest the email configuration request
     * @param id                        the identifier of the configuration to update
     * @return the updated email configuration
     */
    @PatchMapping("update/{id}")
    public ResponseEntity<EmailConfigurationDto> update(@Valid @NotNull @RequestBody EmailConfigurationRequest emailConfigurationRequest, @NotBlank @PathVariable("id") String id) {
        EmailConfigurationDto updatedConfiguration = emailServiceBridge.update(emailConfigurationRequest, UUID.fromString(id));
        return ResponseEntity.ok(updatedConfiguration);
    }
}
