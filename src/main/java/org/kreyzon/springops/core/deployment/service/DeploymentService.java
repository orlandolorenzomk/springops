package org.kreyzon.springops.core.deployment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kreyzon.springops.common.dto.deployment.DeploymentDto;
import org.kreyzon.springops.common.utils.Constants;
import org.kreyzon.springops.core.application.entity.Application;
import org.kreyzon.springops.core.application.service.ApplicationService;
import org.kreyzon.springops.core.deployment.entity.Deployment;
import org.kreyzon.springops.core.deployment.repository.DeploymentRepository;
import org.springframework.stereotype.Service;

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

    private final ApplicationService applicationService;

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
        Application application = applicationService.getEntityById(deploymentDto.getApplicationId());
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
        Deployment deployment = DeploymentDto.toEntity(deploymentDto);
        return DeploymentDto.fromEntity(deploymentRepository.save(deployment));
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
        Application application = applicationService.getEntityById(applicationId);
        return deploymentRepository.findByApplicationAndStatus(application, Constants.STATUS_RUNNING);
    }
}