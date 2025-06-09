package org.kreyzon.springops.core.deployment.service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kreyzon.springops.common.dto.deployment.DeploymentDto;
import org.kreyzon.springops.common.utils.Constants;
import org.kreyzon.springops.core.application.entity.Application;
import org.kreyzon.springops.core.application.service.ApplicationLookupService;
import org.kreyzon.springops.core.deployment.entity.Deployment;
import org.kreyzon.springops.core.deployment.repository.DeploymentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Comparator;
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

    /**
     * Finds a deployment by its ID.
     *
     * @param id the ID of the deployment to find
     * @return the DeploymentDto representing the found deployment
     * @throws IllegalArgumentException if the deployment with the given ID does not exist
     */
    public DeploymentDto findById(Integer id) {
        return deploymentRepository.findById(id)
                .map(DeploymentDto::fromEntity)
                .orElseThrow(() -> new IllegalArgumentException("Deployment with ID '" + id + "' does not exist"));
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
     * @throws IllegalArgumentException if the deployment with the given ID does not exist
     */
    public DeploymentDto update(DeploymentDto deploymentDto) {
        if (!deploymentRepository.existsById(deploymentDto.getId())) {
            throw new IllegalArgumentException("Deployment with ID '" + deploymentDto.getId() + "' does not exist");
        }

        Deployment existingDeployment = deploymentRepository.findById(deploymentDto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Deployment with ID '" + deploymentDto.getId() + "' does not exist"));

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
     * @throws IllegalArgumentException if the deployment with the given ID does not exist
     */
    public void deleteById(Integer id) {
        if (!deploymentRepository.existsById(id)) {
            throw new IllegalArgumentException("Deployment with ID '" + id + "' does not exist");
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
        return deploymentRepository.findByApplicationAndStatus(application, Constants.STATUS_RUNNING);
    }

    /**
     * Finds a deployment by its process ID (PID).
     *
     * @param pid the process ID of the deployment to find
     * @return the Deployment entity representing the found deployment
     * @throws IllegalArgumentException if the deployment with the given PID does not exist
     */
    public Deployment findByPid(Integer pid) {
        return deploymentRepository.findByPid(pid)
                .orElseThrow(() -> new IllegalArgumentException("Deployment with PID '" + pid + "' does not exist"));
    }

    /**
     * Finds the latest running deployment for a specific application.
     *
     * @param applicationId the ID of the application to find the latest running deployment for
     * @return the Deployment entity representing the latest running deployment for the specified application
     * @throws IllegalArgumentException if no running deployments are found for the specified application
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
}