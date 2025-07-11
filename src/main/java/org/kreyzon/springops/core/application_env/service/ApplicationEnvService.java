package org.kreyzon.springops.core.application_env.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kreyzon.springops.common.dto.application_env.ApplicationEnvDto;
import org.kreyzon.springops.common.exception.SkipEnvSaveException;
import org.kreyzon.springops.common.exception.SpringOpsException;
import org.kreyzon.springops.common.utils.EncryptionUtils;
import org.kreyzon.springops.config.ApplicationConfig;
import org.kreyzon.springops.config.annotations.Audit;
import org.kreyzon.springops.config.annotations.SensibleAudit;
import org.kreyzon.springops.core.application.entity.Application;
import org.kreyzon.springops.core.application.service.ApplicationLookupService;
import org.kreyzon.springops.core.application_env.entity.ApplicationEnv;
import org.kreyzon.springops.core.application_env.repository.ApplicationEnvRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Service class for managing application environment configurations.
 * This service is responsible for handling operations related to application environment variables,
 * such as creating, updating, deleting, and retrieving environment variables for applications.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationEnvService {

    private final ApplicationEnvRepository applicationEnvRepository;

    private final ApplicationConfig applicationConfig;

    private final ApplicationLookupService applicationLookupService;

    /**
     * Finds an ApplicationEnv by its ID.
     *
     * @param applicationId the ID of the Application
     * @return the ApplicationEnvDto if found, null otherwise
     */
    public List<ApplicationEnvDto> findByApplicationId(Integer applicationId) {
        log.info("Finding ApplicationEnv by application ID: {}", applicationId);

        Application application = applicationLookupService.findEntityById(applicationId);

        List<ApplicationEnv> applicationEnvs = applicationEnvRepository.findByApplication(application);
        if (applicationEnvs.isEmpty()) {
            log.warn("No ApplicationEnv found for application ID: {}", applicationId);
            return List.of();
        }

        log.info("Found {} ApplicationEnv(s) for application ID: {}", applicationEnvs.size(), applicationId);
        return applicationEnvs.stream()
                .map(ApplicationEnvDto::fromEntity)
                .toList();
    }

    /**
     * Saves multiple ApplicationEnv objects.
     *
     * @param applicationEnvDtoList the list of ApplicationEnvDto to save
     * @return a list of saved ApplicationEnvDto
     * @throws SpringOpsException with {@link HttpStatus#FORBIDDEN} if the maximum number of environment variables exceeds the limit
     * @throws SpringOpsException with {@link HttpStatus#INTERNAL_SERVER_ERROR} if an error occurs during saving
     */
    @Transactional
    @SensibleAudit
    public List<ApplicationEnvDto> save(Integer applicationId, List<ApplicationEnvDto> applicationEnvDtoList) {
        log.info("Saving environment variables");

        Application application = applicationLookupService.findEntityById(applicationId);
        List<ApplicationEnv> existingEnvs = applicationEnvRepository.findByApplication(application);

        if (existingEnvs.size() > applicationConfig.getMaximumEnvFilesPerApplication()) {
            log.error("Maximum environment variables limit exceeded for application: {}", application.getName());
            throw new SpringOpsException("Maximum environment variables limit exceeded for application: " + application.getName(), HttpStatus.FORBIDDEN);
        }

        return applicationEnvDtoList.stream().map(applicationEnvDto -> {
            try {
                String name = applicationEnvDto.getName();
                String rawValue = applicationEnvDto.getValue();

                String encryptedValue = EncryptionUtils.encrypt(
                        rawValue,
                        applicationConfig.getSecret(),
                        applicationConfig.getAlgorithm()
                );

                Optional<ApplicationEnv> existing = existingEnvs.stream()
                        .filter(env -> env.getName().equals(name))
                        .findFirst();

                if (existing.isPresent() && existing.get().getValue().equals(encryptedValue)) {
                    log.info("Env '{}' with same encrypted value already exists. Skipping.", name);
                    return null;
                }

                existing.ifPresent(env -> {
                    log.info("Deleting old env '{}' with different value.", name);
                    applicationEnvRepository.delete(env);
                });

                ApplicationEnv entity = ApplicationEnv.builder()
                        .name(name)
                        .value(encryptedValue)
                        .createdAt(Instant.now())
                        .application(application)
                        .build();

                ApplicationEnv savedEntity = applicationEnvRepository.save(entity);
                log.info("Saved ApplicationEnv: {}", savedEntity.getName());
                return ApplicationEnvDto.fromEntity(savedEntity);

            } catch (Exception e) {
                log.error("Error saving ApplicationEnv: {}", e.getMessage());
                throw new SpringOpsException("Failed to save ApplicationEnv: " + applicationEnvDto.getName() + "\n " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }).filter(Objects::nonNull).toList();
    }

    /**
     * Finds all ApplicationEnvs.
     *
     * @return a list of ApplicationEnvDto
     */
    @SensibleAudit
    public ApplicationEnvDto delete(Integer id) {
        log.info("Deleting ApplicationEnv with ID: {}", id);
        ApplicationEnvDto applicationEnvDto = findById(id);
        if (applicationEnvDto != null) {
            applicationEnvRepository.deleteById(id);
            return applicationEnvDto;
        }
        return null;
    }

    /**
     * Finds an ApplicationEnv by its ID.
     *
     * @param id the ID of the ApplicationEnv
     * @return the ApplicationEnvDto if found, null otherwise
     */
    public ApplicationEnvDto findById(Integer id) {
        log.info("Finding ApplicationEnv by ID: {}", id);
        return applicationEnvRepository.findById(id)
                .map(ApplicationEnvDto::fromEntity)
                .orElse(null);
    }
}
