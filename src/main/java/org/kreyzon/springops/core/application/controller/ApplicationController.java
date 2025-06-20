package org.kreyzon.springops.core.application.controller;

import lombok.RequiredArgsConstructor;
import org.kreyzon.springops.common.dto.application.ApplicationDto;
import org.kreyzon.springops.common.dto.application.ApplicationRunDto;
import org.kreyzon.springops.core.application.service.ApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * Represents the controller layer for managing Application entities.
 * This controller provides RESTful endpoints to interact with the Application service.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@RestController
@RequestMapping("/applications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ApplicationController {

    private final ApplicationService applicationService;

    /**
     * Fetches an Application by its ID.
     *
     * @param id the ID of the Application to fetch
     * @return a ResponseEntity containing the ApplicationDto, or a 404 status if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApplicationDto> findById(@PathVariable Integer id) {
        ApplicationDto applicationDto = applicationService.findById(id);
        return applicationDto != null ? ResponseEntity.ok(applicationDto) : ResponseEntity.notFound().build();
    }

    /**
     * Fetches all Applications.
     *
     * @return a ResponseEntity containing a list of ApplicationDto
     */
    @GetMapping
    public ResponseEntity<List<ApplicationDto>> findAll() {
        List<ApplicationDto> applications = applicationService.findAll();
        return ResponseEntity.ok(applications);
    }

    /**
     * Creates a new Application.
     *
     * @param applicationDto the ApplicationDto to create
     * @return a ResponseEntity containing the created ApplicationDto
     */
    @PostMapping
    public ResponseEntity<ApplicationDto> save(@RequestBody ApplicationDto applicationDto) {
        ApplicationDto savedApplication = applicationService.save(applicationDto);
        return ResponseEntity.ok(savedApplication);
    }

    /**
     * Updates an existing Application.
     *
     * @param id the ID of the Application to update
     * @param applicationDto the ApplicationDto containing updated data
     * @return a ResponseEntity containing the updated ApplicationDto
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApplicationDto> update(@PathVariable Integer id, @RequestBody ApplicationDto applicationDto) {
        ApplicationDto updatedApplication = applicationService.update(id, applicationDto);
        return ResponseEntity.ok(updatedApplication);
    }

    /**
     * Deletes an Application by its ID.
     *
     * @param id the ID of the Application to delete
     * @return a ResponseEntity with a 204 status if deletion is successful
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Integer id) {
        applicationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Runs an Application by its ID.
     *
     * @param applicationId the ID of the Application to run
     * @return a ResponseEntity containing the ApplicationRunDto with run details
     */
    @PostMapping("/{applicationId}/dependencies")
    public ResponseEntity<Set<Integer>> updateDependencies(@PathVariable Integer applicationId,
                                                           @RequestParam(required = false) Set<Integer> dependenciesId) {
        Set<Integer> dependencies = applicationService.updateDependencies(applicationId, dependenciesId);
        return ResponseEntity.ok(dependencies);
    }

    /**
     * Fetches the dependencies of an Application by its ID.
     *
     * @param applicationId the ID of the Application to fetch dependencies for
     * @return a ResponseEntity containing a set of dependent Application IDs
     */
    @GetMapping("/{applicationId}/dependencies")
    public ResponseEntity<Set<Integer>> getDependencies(@PathVariable Integer applicationId) {
        Set<Integer> dependencies = applicationService.findDependentApplications(applicationId);
        return ResponseEntity.ok(dependencies);
    }
}