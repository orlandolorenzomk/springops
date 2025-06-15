package org.kreyzon.springops.core.email.service;

import lombok.RequiredArgsConstructor;
import org.kreyzon.springops.common.exception.SpringOpsException;
import org.kreyzon.springops.core.email.dto.EmailConfigurationDto;
import org.kreyzon.springops.core.email.entity.EmailConfiguration;
import org.kreyzon.springops.core.email.entity.MailjetConfiguration;
import org.kreyzon.springops.core.email.entity.SmtpConfiguration;
import org.kreyzon.springops.core.email.enums.EmailProvider;
import org.kreyzon.springops.core.email.repository.EmailConfigurationRepository;
import org.kreyzon.springops.core.email.request.EmailConfigurationRequest;
import org.kreyzon.springops.core.email.request.MailjetConfigurationRequest;
import org.kreyzon.springops.core.email.request.SmtpConfigurationRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service bridge for handling email configuration requests.
 * Delegates to specific services based on the type of email configuration request.
 * Supports saving and updating configurations, including migration between providers.
 *
 * @author Domenico Ferraro
 */
@Service
@RequiredArgsConstructor
public class EmailServiceBridge {

    private final SmtpService smtpService;
    private final MailjetService mailjetService;
    private final EmailConfigurationRepository emailConfigurationRepository;

    /**
     * Saves a new email configuration based on the request type.
     * Delegates to the appropriate service for saving the configuration.
     *
     * @param request The email configuration request
     * @return The saved email configuration DTO
     *
     * @throws SpringOpsException with {@link HttpStatus#BAD_REQUEST} if the request type is unsupported
     */
    @Transactional
    public EmailConfigurationDto save(EmailConfigurationRequest request) {
        if (request instanceof SmtpConfigurationRequest smtpConfig) {
            return smtpService.save(smtpConfig);
        } else if (request instanceof MailjetConfigurationRequest mailjetConfig) {
            return mailjetService.save(mailjetConfig);
        } else {
            throw new SpringOpsException("Unsupported email configuration type", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Retrieves an email configuration by its identifier.
     *
     * @param id The identifier of the email configuration
     * @return The email configuration DTO
     *
     * @throws SpringOpsException with {@link HttpStatus#NOT_FOUND} if the configuration is not found
     * @throws SpringOpsException with {@link HttpStatus#BAD_REQUEST} if the configuration type is unsupported
     */
    @Transactional(readOnly = true)
    public EmailConfigurationDto findById(UUID id) {
        EmailConfiguration emailConfiguration = emailConfigurationRepository.findById(id)
                .orElseThrow(() -> new SpringOpsException("Email configuration not found", HttpStatus.NOT_FOUND));

        if (emailConfiguration instanceof SmtpConfiguration) {
            return smtpService.toDto(emailConfiguration);
        } else if (emailConfiguration instanceof MailjetConfiguration) {
            return mailjetService.toDto(emailConfiguration);
        }
        throw new SpringOpsException("Unsupported email configuration type", HttpStatus.BAD_REQUEST);
    }

    /**
     * Updates an existing email configuration based on the request type.
     * If the request type matches the existing configuration type, it updates in place.
     * If the request type differs, it migrates the configuration to the new type.
     *
     * @param request The email configuration request
     * @param id The identifier of the existing configuration to update
     * @return The updated email configuration DTO
     *
     * @throws SpringOpsException with {@link HttpStatus#NOT_FOUND} if the configuration is not found
     */
    @Transactional
    public EmailConfigurationDto update(EmailConfigurationRequest request, UUID id) {
        EmailConfiguration existingConfig = emailConfigurationRepository.findById(id)
                .orElseThrow(() -> new SpringOpsException("Email configuration not found", HttpStatus.NOT_FOUND));
        
        //Updating the existing configuration keeping the same provider type
        if (request instanceof SmtpConfigurationRequest && existingConfig.getEmailProvider() == EmailProvider.SMTP) {
            return smtpService.update((SmtpConfigurationRequest) request, existingConfig);
        } else if (request instanceof MailjetConfigurationRequest && existingConfig.getEmailProvider() == EmailProvider.MAILJET) {
            return mailjetService.update((MailjetConfigurationRequest) request, existingConfig);
        }
        // If the request type does not match the existing configuration type, migrate the configuration
        return migrateConfiguration(request, existingConfig);
    }

    /**
     * Migrates an existing email configuration to a new type.
     * Deletes the old configuration and saves the new one based on the request type.
     *
     * @param request The new email configuration request
     * @param existingConfig The existing email configuration to be migrated
     * @return The saved email configuration DTO
     *
     * @throws SpringOpsException with {@link HttpStatus#BAD_REQUEST} if the migration is not supported
     *
     */
    @Transactional
    private EmailConfigurationDto migrateConfiguration(EmailConfigurationRequest request, EmailConfiguration existingConfig) {
        this.emailConfigurationRepository.delete(existingConfig);
        this.emailConfigurationRepository.flush();
        if(existingConfig instanceof SmtpConfiguration && request instanceof MailjetConfigurationRequest){
            return mailjetService.save((MailjetConfigurationRequest) request);
        }
        else if(existingConfig instanceof MailjetConfiguration && request instanceof SmtpConfigurationRequest){
            return smtpService.save((SmtpConfigurationRequest) request);
        }
        throw new SpringOpsException("Cannot migrate from " + existingConfig.getEmailProvider() + " to " + request.getClass().getSimpleName(), HttpStatus.BAD_REQUEST);

    }


}
