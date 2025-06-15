package org.kreyzon.springops.core.email.service;

import org.kreyzon.springops.core.email.dto.EmailConfigurationDto;
import org.kreyzon.springops.core.email.dto.MailjetConfigurationDto;
import org.kreyzon.springops.core.email.entity.EmailConfiguration;
import org.kreyzon.springops.core.email.mapper.EmailConfigurationMapper;
import org.kreyzon.springops.core.email.entity.MailjetConfiguration;
import org.kreyzon.springops.core.email.repository.MailjetConfigurationRepository;
import org.kreyzon.springops.core.email.request.MailjetConfigurationRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import org.kreyzon.springops.common.exception.SpringOpsException;

/**
 * Service for handling Mailjet email configurations.
 * Provides methods to save, update, and delete Mailjet configurations.
 * Extends the base EmailService for common functionality.
 *
 * @author Domenico Ferraro
 */
@Service
public class MailjetService extends EmailService {

    private final MailjetConfigurationRepository mailjetConfigurationRepository;

    public MailjetService(MailjetConfigurationRepository mailjetConfigurationRepository,
                          EmailConfigurationMapper emailConfigurationMapper) {
        super(emailConfigurationMapper);
        this.mailjetConfigurationRepository = mailjetConfigurationRepository;
    }

    public MailjetConfiguration findById(UUID id) {
        return this.mailjetConfigurationRepository.findById(id)
                .orElseThrow(() -> new SpringOpsException("Mailjet configuration not found", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public void deleteById(String id) {
        UUID uuid = UUID.fromString(id);
        this.mailjetConfigurationRepository.deleteById(uuid);
    }

    @Transactional
    public void deleteById(UUID id) {
        this.mailjetConfigurationRepository.deleteById(id);
    }


    @Transactional
    public EmailConfigurationDto save(MailjetConfigurationRequest emailConfigurationRequest) {
        MailjetConfiguration mailjetConfiguration = mapper.fromRequest(emailConfigurationRequest);
        mailjetConfiguration.setCreatedAt(Instant.now());
        return mapper.toDto(mailjetConfigurationRepository.save(mailjetConfiguration));
    }

    @Transactional
    public EmailConfigurationDto update(MailjetConfigurationRequest emailConfigurationRequest, EmailConfiguration existingConfig) {
        MailjetConfiguration mailjetConfig = mapper.fromRequest(emailConfigurationRequest);
        mailjetConfig.setId(existingConfig.getId());
        mailjetConfig.setCreatedAt(existingConfig.getCreatedAt());
        mailjetConfig.setUpdatedAt(Instant.now());
        return mapper.toDto(mailjetConfigurationRepository.save(mailjetConfig));
    }
}
