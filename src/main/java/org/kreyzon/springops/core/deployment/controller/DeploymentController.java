package org.kreyzon.springops.core.deployment.controller;

import lombok.RequiredArgsConstructor;
import org.kreyzon.springops.common.dto.deployment.DeploymentDto;
import org.kreyzon.springops.core.deployment.service.DeploymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing deployments.
 * This controller provides endpoints for CRUD operations on deployments.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@RestController
@RequestMapping("/deployments")
@RequiredArgsConstructor
public class DeploymentController {

    private final DeploymentService deploymentService;

    /**
     * Retrieves a deployment by its ID.
     *
     * @param id the ID of the deployment to retrieve
     * @return the DeploymentDto representing the found deployment
     */
    @GetMapping("/{id}")
    public ResponseEntity<DeploymentDto> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(deploymentService.findById(id));
    }

    /**
     * Retrieves all deployments.
     *
     * @return a list of DeploymentDto representing all deployments
     */
    @GetMapping
    public ResponseEntity<List<DeploymentDto>> findAll() {
        return ResponseEntity.ok(deploymentService.findAll());
    }

    /**
     * Creates a new deployment.
     *
     * @param deploymentDto the DeploymentDto representing the deployment to create
     * @return the DeploymentDto representing the created deployment
     */
    @PostMapping
    public ResponseEntity<DeploymentDto> save(@RequestBody DeploymentDto deploymentDto) {
        return ResponseEntity.ok(deploymentService.save(deploymentDto));
    }

    /**
     * Updates an existing deployment.
     *
     * @param deploymentDto the DeploymentDto representing the deployment to update
     * @return the DeploymentDto representing the updated deployment
     */
    @PutMapping
    public ResponseEntity<DeploymentDto> update(@RequestBody DeploymentDto deploymentDto) {
        return ResponseEntity.ok(deploymentService.update(deploymentDto));
    }

    /**
     * Deletes a deployment by its ID.
     *
     * @param id the ID of the deployment to delete
     * @return a ResponseEntity indicating the result of the operation
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Integer id) {
        deploymentService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}