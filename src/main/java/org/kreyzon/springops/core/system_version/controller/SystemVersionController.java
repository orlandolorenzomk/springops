package org.kreyzon.springops.core.system_version.controller;

import lombok.RequiredArgsConstructor;
import org.kreyzon.springops.common.dto.system_version.SystemVersionDto;
import org.kreyzon.springops.core.system_version.service.SystemVersionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing system versions.
 * Provides endpoints for CRUD operations on {@link SystemVersionDto}.
 * Delegates business logic to {@link SystemVersionService}.
 *
 * Base URL: /api/system-versions
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@
 */
@RestController
@RequestMapping("/system-versions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class SystemVersionController {

    private final SystemVersionService systemVersionService;

    /**
     * Retrieves a system version by its ID.
     *
     * @param id the ID of the system version to retrieve
     * @return the corresponding {@link SystemVersionDto}
     */
    @GetMapping("/{id}")
    public ResponseEntity<SystemVersionDto> getSystemVersionById(@PathVariable Integer id) {
        return ResponseEntity.ok(systemVersionService.findById(id));
    }

    /**
     * Retrieves all system versions.
     *
     * @return a list of {@link SystemVersionDto} representing all system versions
     */
    @GetMapping
    public ResponseEntity<List<SystemVersionDto>> getAllSystemVersions() {
        return ResponseEntity.ok(systemVersionService.findAll());
    }

    /**
     * Creates a new system version.
     *
     * @param systemVersionDto the {@link SystemVersionDto} to create
     * @return the created {@link SystemVersionDto}
     */
    @PostMapping
    public ResponseEntity<SystemVersionDto> createSystemVersion(@RequestBody SystemVersionDto systemVersionDto) {
        return ResponseEntity.ok(systemVersionService.save(systemVersionDto));
    }

    /**
     * Updates an existing system version.
     *
     * @param id the ID of the system version to update
     * @param systemVersionDto the updated {@link SystemVersionDto}
     * @return the updated {@link SystemVersionDto}
     */
    @PutMapping("/{id}")
    public ResponseEntity<SystemVersionDto> updateSystemVersion(
            @PathVariable Integer id, @RequestBody SystemVersionDto systemVersionDto) {
        return ResponseEntity.ok(systemVersionService.update(id, systemVersionDto));
    }

    /**
     * Deletes a system version by its ID.
     *
     * @param id the ID of the system version to delete
     * @return a response indicating the deletion status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSystemVersion(@PathVariable Integer id) {
        systemVersionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}