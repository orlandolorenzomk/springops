package org.kreyzon.springops.core.deployment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kreyzon.springops.core.deployment.entity.Deployment;
import org.kreyzon.springops.core.deployment.repository.DeploymentRepository;
import org.springframework.stereotype.Service;

/**
 * Service for looking up deployments.
 * This service is responsible for handling deployment-related operations,
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeploymentLookupService {

    private final DeploymentRepository deploymentRepository;

    /**
     * Finds a deployment by its ID.
     *
     * @param id the ID of the deployment to find
     * @return the Deployment entity if found
     * @throws IllegalArgumentException if no deployment is found with the given ID
     */
    public Deployment findEntityById(Integer id) {
        log.info("Looking up deployment with ID: {}", id);
        return deploymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Deployment not found with ID: " + id));
    }
}
