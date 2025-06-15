package org.kreyzon.springops.core.deployment.controller;

import lombok.RequiredArgsConstructor;
import org.kreyzon.springops.common.dto.deployment.DeploymentDto;
import org.kreyzon.springops.core.deployment.service.DeploymentService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Paths;
import java.time.LocalDate;
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
@CrossOrigin(origins = "*", allowedHeaders = "*")
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

    /**
     * Searches for deployments based on application ID and creation date.
     *
     * @param applicationId the ID of the application to filter deployments by (optional)
     * @param createdDate   the creation date to filter deployments by (optional)
     * @param page          the page number for pagination (default is 0)
     * @param size          the size of each page for pagination (default is 10)
     * @return a paginated list of DeploymentDto matching the search criteria
     */
    @GetMapping("/search")
    public ResponseEntity<Page<DeploymentDto>> searchDeployments(
            @RequestParam(required = false) Integer applicationId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(deploymentService.searchDeployments(applicationId, createdDate, page, size));
    }

    /**
     * Downloads a log file for a deployment.
     *
     * @param filename the name of the log file to download
     * @return a ResponseEntity containing the log file as a ByteArrayResource
     */
    @GetMapping("/logs")
    public ResponseEntity<ByteArrayResource> downloadLog(@RequestParam String filename) {
        byte[] content = deploymentService.downloadLogFile(filename);
        ByteArrayResource resource = new ByteArrayResource(content);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + Paths.get(filename).getFileName())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(content.length)
                .body(resource);
    }

    /**
     * Updates the notes of a deployment.
     *
     * @param id    the ID of the deployment to update
     * @param value the new notes value to set
     * @return the updated DeploymentDto
     */
    @PatchMapping("/{id}/notes")
    public ResponseEntity<DeploymentDto> updateNotes(@PathVariable Integer id, @RequestParam String value) {
        DeploymentDto updatedDeployment = deploymentService.updateNotes(id, value);
        return ResponseEntity.ok(updatedDeployment);
    }
}