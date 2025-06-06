package org.kreyzon.springops.core.system_version.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kreyzon.springops.common.dto.system_version.SystemVersionDto;
import org.kreyzon.springops.core.system_version.entity.SystemVersion;
import org.kreyzon.springops.core.system_version.repository.SystemVersionRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Service class for managing system versions.
 * Provides methods for CRUD operations and business logic related to {@link SystemVersion}.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SystemVersionService {

    private final SystemVersionRepository systemVersionRepository;

    /**
     * Finds a system version by its ID.
     *
     * @param id the ID of the system version to find
     * @return the corresponding {@link SystemVersionDto}
     * @throws IllegalArgumentException if no system version is found with the given ID
     */
    public SystemVersionDto findById(Integer id) {
        log.info("Fetching system version with ID: {}", id);
        return systemVersionRepository.findById(id)
                .map(SystemVersionDto::fromEntity)
                .orElseThrow(() -> new IllegalArgumentException("System version not found with ID: " + id));
    }

    /**
     * Retrieves all system versions.
     *
     * @return a list of {@link SystemVersionDto} representing all system versions
     */
    public List<SystemVersionDto> findAll() {
        log.info("Fetching all system versions");
        return systemVersionRepository.findAll().stream()
                .map(SystemVersionDto::fromEntity)
                .toList();
    }

    /**
     * Saves a new system version.
     *
     * @param systemVersionDto the {@link SystemVersionDto} to save
     * @return the saved {@link SystemVersionDto}
     */
    public SystemVersionDto save(SystemVersionDto systemVersionDto) {
        log.info("Saving system version: {}", systemVersionDto);
        SystemVersion entity = SystemVersionDto.toEntity(systemVersionDto);
        entity.setCreatedAt(Instant.now());
        SystemVersion savedEntity = systemVersionRepository.save(entity);
        log.info("System version saved with ID: {}", savedEntity.getId());
        return SystemVersionDto.fromEntity(savedEntity);
    }

    /**
     * Updates an existing system version.
     *
     * @param id the ID of the system version to update
     * @param systemVersionDto the updated {@link SystemVersionDto}
     * @return the updated {@link SystemVersionDto}
     * @throws IllegalArgumentException if no system version is found with the given ID
     */
    public SystemVersionDto update(Integer id, SystemVersionDto systemVersionDto) {
        log.info("Updating system version with ID: {}", id);
        SystemVersion existingEntity = systemVersionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("System version not found with ID: " + id));

        existingEntity.setType(systemVersionDto.getType());
        existingEntity.setVersion(systemVersionDto.getVersion());
        existingEntity.setPath(systemVersionDto.getPath());
        existingEntity.setCreatedAt(Instant.now());

        SystemVersion updatedEntity = SystemVersionDto.toEntity(systemVersionDto);
        log.info("System version updated with ID: {}", id);

        return SystemVersionDto.fromEntity(updatedEntity);
    }

    /**
     * Deletes a system version by its ID.
     *
     * @param id the ID of the system version to delete
     * @throws IllegalArgumentException if no system version is found with the given ID
     */
    public void delete(Integer id) {
        log.info("Deleting system version with ID: {}", id);
        if (!systemVersionRepository.existsById(id)) {
            throw new IllegalArgumentException("System version not found with ID: " + id);
        }
        systemVersionRepository.deleteById(id);
        log.info("System version with ID: {} deleted successfully", id);
    }
}