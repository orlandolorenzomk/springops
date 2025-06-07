package org.kreyzon.springops.core.application_env.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kreyzon.springops.common.dto.application.ApplicationDto;
import org.kreyzon.springops.common.dto.application_env.ApplicationEnvDto;
import org.kreyzon.springops.common.utils.EncryptionUtils;
import org.kreyzon.springops.config.ApplicationConfig;
import org.kreyzon.springops.core.application.service.ApplicationService;
import org.kreyzon.springops.core.application_env.entity.ApplicationEnv;
import org.kreyzon.springops.core.application_env.repository.ApplicationEnvRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

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

    private final ApplicationService applicationService;

    /**
     * Finds an ApplicationEnv by its ID.
     *
     * @param applicationId the ID of the Application
     * @return the ApplicationEnvDto if found, null otherwise
     */
    public ApplicationEnvDto findByApplicationId(Integer applicationId) {
        log.info("Finding ApplicationEnv by application ID: {}", applicationId);

        ApplicationDto application = applicationService.findById(applicationId);

        List<ApplicationEnv> applicationEnvs = applicationEnvRepository.findByApplication(ApplicationDto.toEntity(application));
        return applicationEnvs.stream()
                .map(ApplicationEnvDto::fromEntity)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("ApplicationEnv not found for application ID: " + applicationId));
    }

    /**
     * Saves multiple ApplicationEnv objects.
     *
     * @param applicationEnvDtoList the list of ApplicationEnvDto to save
     * @return a list of saved ApplicationEnvDto
     */
    public List<ApplicationEnvDto> save(List<ApplicationEnvDto> applicationEnvDtoList) throws Exception {
        log.info("Saving multiple ApplicationEnv objects" + " with size: {}", applicationEnvDtoList.size());

        ApplicationDto applicationDto = applicationService.findById(applicationEnvDtoList.get(0).getApplicationId());
        List<ApplicationEnv> existingEnvs = applicationEnvRepository.findByApplication(
                ApplicationDto.toEntity(applicationDto)
        );
        if (existingEnvs.size() > applicationConfig.getMaximumEnvFilesPerApplication()) {
            log.error("Maximum environment variables limit exceeded for application: {}", applicationDto.getName());
            throw new RuntimeException("Maximum environment variables limit exceeded for application: " + applicationDto.getName());
        }

        return applicationEnvDtoList.stream().map(applicationEnvDto -> {
            try {
                ApplicationEnv entity = ApplicationEnv
                        .builder()
                        .name(applicationEnvDto.getName())
                        .value(EncryptionUtils.encrypt(
                                applicationEnvDto.getValue(),
                                applicationConfig.getSecret(),
                                applicationConfig.getAlgorithm()
                        ))
                        .createdAt(Instant.now())
                        .application(ApplicationDto.toEntity(applicationDto))
                        .build();

                entity.setCreatedAt(Instant.now());
                ApplicationEnv savedEntity = applicationEnvRepository.save(entity);
                log.info("Saved ApplicationEnv: {}", savedEntity.getName());
                return ApplicationEnvDto.fromEntity(savedEntity);
            } catch (Exception e) {
                log.error("Error saving ApplicationEnv: {}", e.getMessage());
                throw new RuntimeException("Failed to save ApplicationEnv: " + applicationEnvDto.getName(), e);
            }
        }).toList();
    }

    /**
     * Finds all ApplicationEnvs.
     *
     * @return a list of ApplicationEnvDto
     */
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
