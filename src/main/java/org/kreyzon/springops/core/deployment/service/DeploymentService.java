package org.kreyzon.springops.core.deployment.service;

import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kreyzon.springops.common.dto.deployment.DeploymentDto;
import org.kreyzon.springops.common.enums.DeploymentStatus;
import org.kreyzon.springops.common.exception.SpringOpsException;
import org.kreyzon.springops.common.utils.DeploymentUtils;
import org.kreyzon.springops.config.ApplicationConfig;
import org.kreyzon.springops.config.annotations.Audit;
import org.kreyzon.springops.core.application.entity.Application;
import org.kreyzon.springops.core.application.service.ApplicationLookupService;
import org.kreyzon.springops.core.deployment.entity.Deployment;
import org.kreyzon.springops.core.deployment.repository.DeploymentRepository;
import org.kreyzon.springops.setup.domain.Setup;
import org.kreyzon.springops.setup.service.SetupService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Service class for managing deployments.
 * This class is responsible for handling deployment-related operations such as finding, saving, updating, and deleting deployments.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeploymentService {

    private final DeploymentRepository deploymentRepository;

    private final ApplicationLookupService applicationLookupService;

    private final SetupService setupService;

    private final ApplicationConfig applicationConfig;

    /**
     * Finds a deployment by its ID.
     *
     * @param id the ID of the deployment to find
     * @return the DeploymentDto representing the found deployment
     * @throws SpringOpsException with {@link HttpStatus#NOT_FOUND} if the deployment with the given ID does not exist
     */
    public DeploymentDto findById(Integer id) {
        return deploymentRepository.findById(id)
                .map(DeploymentDto::fromEntity)
                .orElseThrow(() -> new SpringOpsException("Deployment with ID '" + id + "' does not exist", HttpStatus.NOT_FOUND));
    }

    /**
     * Finds all deployments.
     *
     * @return a list of DeploymentDto representing all deployments
     */
    public List<DeploymentDto> findAll() {
        return deploymentRepository.findAll()
                .stream()
                .map(DeploymentDto::fromEntity)
                .toList();
    }

    /**
     * Saves a new deployment.
     *
     * @param deploymentDto the DeploymentDto representing the deployment to save
     * @return the DeploymentDto representing the saved deployment
     */
    public DeploymentDto save(DeploymentDto deploymentDto) {
        Deployment deployment = DeploymentDto.toEntity(deploymentDto);
        Application application = applicationLookupService.findEntityById(deploymentDto.getApplicationId());
        deployment.setApplication(application);
        return DeploymentDto.fromEntity(deploymentRepository.save(deployment));
    }

    /**
     * Updates an existing deployment.
     *
     * @param deploymentDto the DeploymentDto representing the deployment to update
     * @return the DeploymentDto representing the updated deployment
     * @throws SpringOpsException if the deployment with the given ID does not exist
     */
    public DeploymentDto update(DeploymentDto deploymentDto) {
        if (!deploymentRepository.existsById(deploymentDto.getId())) {
            throw new SpringOpsException("Deployment with ID '" + deploymentDto.getId() + "' does not exist", HttpStatus.NOT_FOUND);
        }

        // Check if deployment exists
        deploymentRepository.findById(deploymentDto.getId())
                .orElseThrow(() -> new SpringOpsException("Deployment with ID '" + deploymentDto.getId() + "' does not exist", HttpStatus.NOT_FOUND));

        Deployment deployment = DeploymentDto.toEntity(deploymentDto);
        Application application = applicationLookupService.findEntityById(deploymentDto.getApplicationId());
        deployment.setApplication(application);
        deploymentRepository.save(deployment);

        log.info("Updated deployment with ID: {}", deploymentDto.getId());
        return DeploymentDto.fromEntity(deployment);
    }

    /**
     * Deletes a deployment by its ID.
     *
     * @param id the ID of the deployment to delete
     * @throws SpringOpsException with {@link HttpStatus#NOT_FOUND} if the deployment with the given ID does not exist
     */
    @Audit
    public void deleteById(Integer id) {
        if (!deploymentRepository.existsById(id)) {
            throw new SpringOpsException("Deployment with ID '" + id + "' does not exist", HttpStatus.NOT_FOUND);
        }
        deploymentRepository.deleteById(id);
    }

    /**
     * Finds all running deployments for a specific application.
     *
     * @param applicationId the ID of the application to find running deployments for
     * @return a list of Deployment entities representing the running deployments for the specified application
     */

    public List<Deployment> findByApplicationId(Integer applicationId) {
        Application application = applicationLookupService.findEntityById(applicationId);
        return deploymentRepository.findByApplicationAndStatus(application, DeploymentStatus.RUNNING);
    }

    /**
     * Finds a deployment by its process ID (PID).
     *
     * @param pid the process ID of the deployment to find
     * @return the Deployment entity representing the found deployment
     * @throws SpringOpsException with {@link HttpStatus#NOT_FOUND} if the deployment with the given PID does not exist
     */
    public Deployment findByPid(Integer pid) {
        return deploymentRepository.findByPid(pid)
                .orElseThrow(() -> new SpringOpsException("Deployment with PID '" + pid + "' does not exist", HttpStatus.NOT_FOUND));
    }

    /**
     * Finds the latest running deployment for a specific application.
     *
     * @param applicationId the ID of the application to find the latest running deployment for
     * @return the Deployment entity representing the latest running deployment for the specified application
     * @throws SpringOpsException with {@link HttpStatus#NOT_FOUND} if no running deployments are found for the application
     */
    public Deployment findLatestByApplicationId(Integer applicationId) {
        return deploymentRepository.findByCreatedAtDesc(applicationId)
                .orElse(null);
    }

    /**
     * Searches for deployments based on application ID and creation date.
     *
     * @param applicationId the ID of the application to filter deployments by
     * @param createdDate   the creation date to filter deployments by
     * @param page          the page number for pagination
     * @param size          the size of each page for pagination
     * @return a paginated list of DeploymentDto representing the found deployments
     */
    public Page<DeploymentDto> searchDeployments(Integer applicationId, LocalDate createdDate, int page, int size) {
        Specification<Deployment> spec = (root, query, cb) -> {
            query.distinct(true);

            Predicate predicate = cb.conjunction();

            if (applicationId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("application").get("id"), applicationId));
            }

            if (createdDate != null) {
                Instant startOfDay = createdDate.atStartOfDay().toInstant(ZoneOffset.UTC);
                Instant endOfDay = createdDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
                predicate = cb.and(predicate, cb.between(root.get("createdAt"), startOfDay, endOfDay));
            }

            return predicate;
        };

        return deploymentRepository.findAll(
                spec,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"))
        ).map(DeploymentDto::fromEntity);
    }

    /**
     * Generates a logs path for a deployment and writes the provided content to that path.
     *
     * @param deploymentId the ID of the deployment for which to generate the logs path
     * @param content      the content to write to the logs file
     * @throws SpringOpsException with {@link HttpStatus#NOT_FOUND} if the deployment with the given ID does not exist
     */
    @Transactional
    public void generateLogsPath(Integer deploymentId, String content) {
        log.info("Generating logs path for deployment with ID: {}", deploymentId);
        Deployment deployment = deploymentRepository.findById(deploymentId)
                .orElseThrow(() -> new SpringOpsException("Deployment with ID '" + deploymentId + "' does not exist", HttpStatus.NOT_FOUND));

        Application application = applicationLookupService.findEntityById(deployment.getApplication().getId());
        Setup setup = setupService.getSetup();

        String logsPath = setup.getFilesRoot() + "/" +
                applicationConfig.getRootDirectoryName() + "/" +
                applicationConfig.getDirectoryApplications() + "/" +
                application.getName().toLowerCase().replace(" ", "-") + "/logs/deploy-" + deployment.getCreatedAt().toString() + ".log";

        deployment.setLogsPath(logsPath);

        deploymentRepository.save(deployment);

        try {
            java.nio.file.Files.writeString(java.nio.file.Paths.get(logsPath), content);
            log.info("Logs written to path: {}", logsPath);
        } catch (java.io.IOException e) {
            log.error("Failed to write logs to path: {}", logsPath, e);
            throw new SpringOpsException("Error writing logs to path", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Downloads a specific log file for the given application.
     *
     * @param filename      the name of the log file to be downloaded
     * @return byte array containing the contents of the log file
     */
    @Audit
    public byte[] downloadLogFile(String filename) {
        log.info("Downloading log file '{}'", filename);

        Path path = Paths.get(filename);

        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            log.error("Log file '{}' not found at path: {}", filename, filename);
            throw new SpringOpsException("Log file not found", HttpStatus.NOT_FOUND);
        }

        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            log.error("Failed to read log file: {}", filename, e);
            throw new RuntimeException("Error reading log file", e);
        }
    }

    /**
     * Updates the notes for a specific deployment.
     *
     * @param deploymentId the ID of the deployment to update
     * @param notes        the new notes to set for the deployment
     * @return the updated DeploymentDto representing the deployment with updated notes
     * @throws SpringOpsException with {@link HttpStatus#NOT_FOUND} if the deployment with the given ID does not exist
     */
    @Audit
    public DeploymentDto updateNotes(Integer deploymentId, String notes) {
        log.info("Updating notes for deployment with ID: {}", deploymentId);
        Deployment deployment = deploymentRepository.findById(deploymentId)
                .orElseThrow(() -> new SpringOpsException("Deployment with ID '" + deploymentId + "' does not exist", HttpStatus.NOT_FOUND));

        deployment.setNotes(notes);
        deploymentRepository.save(deployment);

        log.info("Updated notes for deployment with ID: {}", deploymentId);
        return DeploymentDto.fromEntity(deployment);
    }

    /**
     * Retrieves a list of DeploymentDto for deployments currently marked as RUNNING
     * and whose processes are actually alive.
     *
     * @return list of active running deployment DTOs
     */
    public List<DeploymentDto> findActiveRunningDeployments() {
        return deploymentRepository.findByStatus(DeploymentStatus.RUNNING).stream()
                .filter(deployment -> {
                    boolean alive = DeploymentUtils.isPidRunning(deployment.getPid());
                    if (!alive) {
                        log.warn("PID {} for deployment ID {} is not active; skipping", deployment.getPid(), deployment.getId());
                    }
                    return alive;
                })
                .map(DeploymentDto::fromEntity)
                .toList();
    }
}