package org.kreyzon.springops.core.application.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kreyzon.springops.common.dto.application.ApplicationDto;
import org.kreyzon.springops.common.dto.deployment.DeploymentStatusDto;
import org.kreyzon.springops.common.dto.system_version.SystemVersionDto;
import org.kreyzon.springops.common.exception.SpringOpsException;
import org.kreyzon.springops.common.utils.PortUtils;
import org.kreyzon.springops.config.ApplicationConfig;
import org.kreyzon.springops.config.annotations.Audit;
import org.kreyzon.springops.core.application.entity.Application;
import org.kreyzon.springops.core.application.repository.ApplicationRepository;
import org.kreyzon.springops.core.deployment.service.DeploymentManagerService;
import org.kreyzon.springops.core.system_version.entity.SystemVersion;
import org.kreyzon.springops.core.system_version.service.SystemVersionService;
import org.kreyzon.springops.setup.service.SetupService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents the service layer for managing Application entities.
 * This service provides methods to interact with the Application repository
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationService {

    private final ApplicationRepository applicationRepository;

    private final SystemVersionService systemVersionService;

    private final ApplicationConfig applicationConfig;

    private final SetupService setupService;

    private final DeploymentManagerService deploymentManagerService;

    /**
     * Finds an Application by its ID.
     *
     * @param id the ID of the Application to find
     * @return an ApplicationDto representing the found Application
     * @throws SpringOpsException with {@link HttpStatus#NOT_FOUND} if the Application with the given ID does not exist
     */
    public ApplicationDto findById(Integer id) {
        return applicationRepository.findById(id)
                .map(ApplicationDto::fromEntity)
                .orElseThrow(() -> new SpringOpsException("Application with ID '" + id + "' does not exist", HttpStatus.NOT_FOUND));
    }

    /**
     * Finds all Applications.
     *
     * @return a list of ApplicationDto representing all Applications
     */
    public List<ApplicationDto> findAll() {
        return applicationRepository.findAll().stream()
                .map(ApplicationDto::fromEntity)
                .toList();
    }

    /**
     * Saves a new Application.
     *
     * @param applicationDto the ApplicationDto to save
     * @return the saved ApplicationDto
     * @throws SpringOpsException with {@link HttpStatus#CONFLICT} if an Application with the same name already exists
     */
    @Audit
    public ApplicationDto save(ApplicationDto applicationDto) {
        SystemVersionDto mvnSystemVersion = systemVersionService.findById(applicationDto.getMvnSystemVersionId());
        SystemVersion systemVersion = SystemVersionDto.toEntity(mvnSystemVersion);
        SystemVersionDto javaSystemVersion = systemVersionService.findById(applicationDto.getJavaSystemVersionId());
        SystemVersion javaVersion = SystemVersionDto.toEntity(javaSystemVersion);

        PortUtils.validatePort(applicationDto.getPort());

        if (applicationRepository.existsByName(applicationDto.getName())) {
            log.warn("Application with name '{}' already exists", applicationDto.getName());
            throw new SpringOpsException("Application with name '" + applicationDto.getName() + "' already exists", HttpStatus.CONFLICT);
        }

        Application application = ApplicationDto.toEntity(applicationDto);
        application.setMvnSystemVersion(systemVersion);
        application.setJavaSystemVersion(javaVersion);
        application.setCreatedAt(java.time.Instant.now());
        application.setFolderRoot(applicationDto.getName().trim().toLowerCase(Locale.ROOT));
        application.setPort(applicationDto.getPort());
        application.setJavaMinimumMemory(
                applicationDto.getJavaMinimumMemory() != null ? applicationDto.getJavaMinimumMemory() : "512m"
        );
        application.setJavaMaximumMemory(
                applicationDto.getJavaMaximumMemory() != null ? applicationDto.getJavaMaximumMemory() : "1024m"
        );
        generateApplicationFolders(application.getName());

        Application savedApplication = applicationRepository.save(application);
        return ApplicationDto.fromEntity(savedApplication);
    }

    /**
     * Updates an existing Application.
     *
     * @param id the ID of the Application to update
     * @param applicationDto the ApplicationDto containing updated data
     * @throws SpringOpsException with {@link HttpStatus#NOT_FOUND} if the Application with the given ID does not exist
     * @throws SpringOpsException with {@link HttpStatus#CONFLICT} if an Application with the same name already exists (for different ID)
     * @throws SpringOpsException with {@link HttpStatus#CONFLICT} if the Application is currently running and cannot be updated
     * @return the updated ApplicationDto
     */
    @Audit
    public ApplicationDto update(Integer id, ApplicationDto applicationDto) {
        SystemVersionDto mvnSystemVersion = systemVersionService.findById(applicationDto.getMvnSystemVersionId());
        SystemVersion systemVersion = SystemVersionDto.toEntity(mvnSystemVersion);
        SystemVersionDto javaSystemVersion = systemVersionService.findById(applicationDto.getJavaSystemVersionId());
        SystemVersion javaVersion = SystemVersionDto.toEntity(javaSystemVersion);

        PortUtils.validatePort(applicationDto.getPort());

        Application existingApplication = applicationRepository.findById(id)
                .orElseThrow(() -> new SpringOpsException("Application with ID '" + id + "' does not exist", HttpStatus.NOT_FOUND));

        DeploymentStatusDto deploymentStatus = deploymentManagerService.getDeploymentStatus(id);
        if (Boolean.TRUE.equals(deploymentStatus.getIsRunning())) {
            log.warn("Application {} with ID '{}' is currently running and cannot be updated", applicationDto.getName(), id);
            throw new SpringOpsException("Application " + applicationDto.getName() + " with ID '" + id + "' is currently running and cannot be updated", HttpStatus.CONFLICT);
        }

        if (applicationRepository.existsByName(applicationDto.getName()) && !existingApplication.getName().equals(applicationDto.getName())) {
            log.warn("Application with name '{}' already exists", applicationDto.getName());
            throw new SpringOpsException("Application with name '" + applicationDto.getName() + "' already exists", HttpStatus.CONFLICT);
        }

        if (!applicationRepository.existsById(id)) {
            log.warn("Application with ID '{}' does not exist", id);
            throw new SpringOpsException("Application with ID '" + id + "' does not exist", HttpStatus.NOT_FOUND);
        }
        if (applicationRepository.existsByName(applicationDto.getName()) && !applicationDto.getId().equals(id)) {
            log.warn("Application with name '{}' already exists", applicationDto.getName());
            throw new SpringOpsException("Application with name '" + applicationDto.getName() + "' already exists", HttpStatus.CONFLICT);
        }
        Application application = ApplicationDto.toEntity(applicationDto);
        application.setId(id);
        application.setMvnSystemVersion(systemVersion);
        application.setJavaSystemVersion(javaVersion);
        application.setFolderRoot(existingApplication.getFolderRoot());
        application.setCreatedAt(existingApplication.getCreatedAt());
        application.setPort(applicationDto.getPort());
        application.setJavaMinimumMemory(
                applicationDto.getJavaMinimumMemory() != null ? applicationDto.getJavaMinimumMemory() : "512m"
        );
        application.setJavaMaximumMemory(
                applicationDto.getJavaMaximumMemory() != null ? applicationDto.getJavaMaximumMemory() : "1024m"
        );

        Application updatedApplication = applicationRepository.save(application);

        return ApplicationDto.fromEntity(updatedApplication);
    }

    /**
     * Deletes an Application by its ID.
     * @throws SpringOpsException with {@link HttpStatus#NOT_FOUND} if the Application with the given ID does not exist
     * @throws SpringOpsException with {@link HttpStatus#CONFLICT} if the Application is currently running and cannot be deleted
     * @param id the ID of the Application to delete
     */
    @Audit
    public void deleteById(Integer id) {
        if (!applicationRepository.existsById(id)) {
            log.warn("Application with ID '{}' does not exist", id);
            throw new SpringOpsException("Application with ID '" + id + "' does not exist", HttpStatus.NOT_FOUND);
        }

        DeploymentStatusDto deploymentStatus = deploymentManagerService.getDeploymentStatus(id);
        if (Boolean.TRUE.equals(deploymentStatus.getIsRunning())) {
            log.warn("Application with ID '{}' is currently running and cannot be updated", id);
            throw new SpringOpsException("Application with ID '" + id + "' is currently running and cannot be updated", HttpStatus.CONFLICT);
        }

        applicationRepository.deleteById(id);
        log.info("Application with ID '{}' deleted successfully", id);
    }

    /**
     * Generates application folders including root, source, and backup directories.
     * Ensures the directories are created if they do not already exist.
     *
     * @param applicationName the name of the application for which folders are generated
     */
    private void generateApplicationFolders(String applicationName) {
        log.info("Generating application folders in: {}", applicationConfig.getRootDirectoryName() + "/" + applicationConfig.getDirectoryApplications());
        applicationName = applicationName.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", "-");

        String rootDirectory = setupService.getSetup().getFilesRoot() + "/" +
                applicationConfig.getRootDirectoryName() + "/" +
                applicationConfig.getDirectoryApplications() + "/" +
                applicationName;

        createDirectory(rootDirectory);

        String sourceDirectory = rootDirectory + "/" + applicationConfig.getDirectorySource();
        String backupDirectory = rootDirectory + "/" + applicationConfig.getDirectoryBackups();
        String logsDirectory = rootDirectory + "/" + applicationConfig.getDirectoryApplicationLogs();

        createDirectory(sourceDirectory);
        createDirectory(backupDirectory);
        createDirectory(logsDirectory);
    }

    /**
     * Creates a directory if it does not exist and logs the result.
     *
     * @param directoryPath the path of the directory to create
     */
    private void createDirectory(String directoryPath) {
        java.io.File directory = new java.io.File(directoryPath);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
                log.info("Created directory: {}", directoryPath);
            } else {
                log.error("Failed to create  directory: {}", directoryPath);
            }
        } else {
            log.info("Directory already exists: {}", directoryPath);
        }
    }

    /**
     * Retrieves an Application entity by its ID.
     *
     * @param applicationId the ID of the Application to retrieve
     * @return the Application entity if found
     * @throws SpringOpsException with {@link HttpStatus#NOT_FOUND} if the Application with the given ID does not exist
     */
    public Application getEntityById(Integer applicationId) {
        return applicationRepository
                .findById(applicationId)
                .orElseThrow(() -> new SpringOpsException("Application with ID '" + applicationId + "' does not exist", HttpStatus.NOT_FOUND));
    }

    /**
     * Updates the dependencies of an Application.
     *
     * @param appId the ID of the Application to update
     * @param dependsOnAppIds a set of IDs representing the Applications that this Application depends on
     * @return a set of IDs representing the updated dependencies
     * @throws SpringOpsException with {@link HttpStatus#NOT_FOUND} if the Application with the given ID does not exist
     */
    @Transactional
    @Audit
    public Set<Integer> updateDependencies(Integer appId, Set<Integer> dependsOnAppIds) {
        log.info("Updating dependencies for Application with ID '{}'", appId);
        Application app = applicationRepository.findById(appId)
                .orElseThrow(() -> {
                    log.warn("Application with ID '{}' does not exist", appId);
                    return new SpringOpsException("Application with ID '" + appId + "' does not exist", HttpStatus.NOT_FOUND);
                });

        app.getDependencies().clear();
        applicationRepository.saveAndFlush(app);

        if (dependsOnAppIds == null || dependsOnAppIds.isEmpty()) {
            log.warn("No dependencies provided for Application with ID '{}'", appId);
            return Set.of();
        }

        Set<Application> dependencies = new HashSet<>(applicationRepository.findAllById(dependsOnAppIds));
        app.setDependencies(dependencies);
        applicationRepository.save(app);

        log.info("Updated dependencies for Application with ID '{}'", appId);
        return app.getDependencies().stream()
                .map(Application::getId)
                .collect(Collectors.toSet());
    }

    /**
     * Finds all dependent Applications for a given Application ID.
     *
     * @param appId the ID of the Application to find dependencies for
     * @return a set of IDs representing the dependent Applications
     * @throws SpringOpsException with {@link HttpStatus#NOT_FOUND} if the Application with the given ID does not exist
     */
    public Set<Integer> findDependentApplications(Integer appId) {
        log.info("Finding dependent applications for Application with ID '{}'", appId);

        if (!applicationRepository.existsById(appId)) {
            log.warn("Application with ID '{}' does not exist", appId);
            throw new SpringOpsException("Application with ID '" + appId + "' does not exist", HttpStatus.NOT_FOUND);
        }

        Application app = applicationRepository.findById(appId).orElseThrow();
        Set<Application> dependencies = app.getDependencies();

        log.info("Found {} dependent applications for Application with ID '{}'", dependencies.size(), appId);
        return dependencies.stream()
                .map(Application::getId)
                .collect(Collectors.toSet());
    }
}
