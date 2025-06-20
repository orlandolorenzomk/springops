package org.kreyzon.springops.email.service;

import io.micrometer.common.util.StringUtils;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.kreyzon.springops.common.exception.SpringOpsException;
import org.kreyzon.springops.config.annotations.Audit;
import org.kreyzon.springops.email.dto.EmailConfigurationDto;
import org.kreyzon.springops.email.entity.EmailConfiguration;
import org.kreyzon.springops.email.mapper.EmailConfigurationMapper;
import org.kreyzon.springops.email.entity.SmtpConfiguration;
import org.kreyzon.springops.email.repository.SmtpConfigurationRepository;
import org.kreyzon.springops.email.request.SmtpConfigurationRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Service for handling SMTP email configurations.
 * Provides methods to save, update, and delete SMTP configurations.
 * Extends the base EmailService for common functionality.
 *
 * @author Domenico Ferraro
 */
@Service
@Slf4j
public class SmtpService extends EmailService {

    private final SmtpConfigurationRepository smtpConfigurationRepository;

    public SmtpService(SmtpConfigurationRepository smtpConfigurationRepository, EmailConfigurationMapper emailConfigurationMapper) {
        super(emailConfigurationMapper);
        this.smtpConfigurationRepository = smtpConfigurationRepository;
    }

    public SmtpConfiguration findById(UUID id) {
        log.info("Retrieving SMTP configuration with ID: {}", id);
        return this.smtpConfigurationRepository.findById(id)
                .orElseThrow(() -> new SpringOpsException("SMTP configuration not found", HttpStatus.NOT_FOUND));
    }

    @Audit
    @Transactional
    public EmailConfigurationDto save(SmtpConfigurationRequest emailConfigurationRequest) {
        log.info("Saving new SMTP configuration: {}", emailConfigurationRequest);
       this.validateAuthOrThrow(emailConfigurationRequest);
        SmtpConfiguration smtpConfiguration = mapper.fromRequest(emailConfigurationRequest);
        smtpConfiguration.setCreatedAt(Instant.now());
        return mapper.toDto(
                smtpConfigurationRepository.save(smtpConfiguration)
        );
    }

    /**
     * Updates an existing SMTP configuration.
     * Validates the authentication details and updates the configuration with the provided request data.
     *
     * @param emailConfigurationRequest the request containing updated SMTP configuration details
     * @param existingConfig the existing SMTP configuration to update
     * @return the updated email configuration DTO
     */
    @Audit
    @Transactional
    public EmailConfigurationDto update(SmtpConfigurationRequest emailConfigurationRequest, EmailConfiguration existingConfig) {
        log.info("Updating SMTP configuration with ID: {}", existingConfig.getId());
        this.validateAuthOrThrow(emailConfigurationRequest);
        SmtpConfiguration smtpConfig = mapper.fromRequest(emailConfigurationRequest);
        smtpConfig.setId(existingConfig.getId());
        smtpConfig.setCreatedAt(existingConfig.getCreatedAt());
        smtpConfig.setUpdatedAt(Instant.now());
        return mapper.toDto(smtpConfigurationRepository.save(smtpConfig));
    }

    /**
     * Validates the authentication details in the SMTP configuration request.
     * Throws an exception if authentication is required but credentials are missing.
     *
     * @param emailConfigurationRequest the SMTP configuration request to validate
     * @throws SpringOpsException with {@link HttpStatus#BAD_REQUEST} if authentication is required but username or password is blank
     */
    private void validateAuthOrThrow(SmtpConfigurationRequest emailConfigurationRequest) {
        if(emailConfigurationRequest.getUseAuth()){
            if(StringUtils.isBlank(emailConfigurationRequest.getUsername()) || StringUtils.isBlank(emailConfigurationRequest.getPassword())) {
                throw new SpringOpsException("Username and password must be provided for authenticated SMTP configurations", HttpStatus.BAD_REQUEST);
            }
        }
    }
}
