package org.kreyzon.springops.core.system_version.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kreyzon.springops.common.dto.system_version.SystemVersionDto;
import org.kreyzon.springops.core.system_version.entity.SystemVersion;
import org.kreyzon.springops.core.system_version.repository.SystemVersionRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

        validateSystemVersion(systemVersionDto);

        if (systemVersionRepository.existsByName(systemVersionDto.getName())) {
            log.warn("System version with name '{}' already exists", systemVersionDto.getName());
            throw new IllegalArgumentException("A system version with name '" + systemVersionDto.getName() + "' already exists.");
        }

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
     * @throws IllegalArgumentException if another system version with the same name exists
     */
    public SystemVersionDto update(Integer id, SystemVersionDto systemVersionDto) {
        log.info("Updating system version with ID: {}", id);
        SystemVersion existingEntity = systemVersionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("System version not found with ID: " + id));

        validateSystemVersion(systemVersionDto);

        if (systemVersionRepository.existsByName(systemVersionDto.getName()) &&
                !existingEntity.getName().equals(systemVersionDto.getName())) {
            log.warn("System version with name '{}' already exists", systemVersionDto.getName());
            throw new IllegalArgumentException("Another system version with name '" + systemVersionDto.getName() + "' already exists.");
        }

        existingEntity.setType(systemVersionDto.getType());
        existingEntity.setVersion(systemVersionDto.getVersion());
        existingEntity.setPath(systemVersionDto.getPath());
        existingEntity.setCreatedAt(Instant.now());

        SystemVersion updatedEntity = systemVersionRepository.save(existingEntity);
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

    /**
     * Finds a system version by its type.
     *
     * @param type the type of the system version to find
     * @return the corresponding {@link SystemVersion}
     * @throws IllegalArgumentException if no system version is found with the given type
     */
    public SystemVersion findByType(String type) {
        log.info("Finding system version by type: {}", type);
        return systemVersionRepository.findByType(type)
                .orElseThrow(() -> new IllegalArgumentException("System version not found with type: " + type));
    }

    /**
     * Validates a system version by checking:
     * 1. If the path exists
     * 2. If the version command executes properly
     *
     * @param systemVersionDto the system version to validate
     * @throws IllegalArgumentException if validation fails
     */
    public void validateSystemVersion(SystemVersionDto systemVersionDto) {
        log.info("Validating system version: {}", systemVersionDto.getName());

        // 1. Check if path exists
        Path path = Paths.get(systemVersionDto.getPath());
        if (!Files.exists(path)) {
            log.error("Path does not exist: {}", systemVersionDto.getPath());
            throw new IllegalArgumentException("Path does not exist: " + systemVersionDto.getPath());
        }

        // 1.5. Validate path ends with /bin for JAVA and MAVEN
        String normalizedPath = path.toString().replace("\\", "/"); // for Windows compatibility
        if (("JAVA".equalsIgnoreCase(systemVersionDto.getType()) ||
                "MAVEN".equalsIgnoreCase(systemVersionDto.getType())) &&
                !normalizedPath.endsWith("/bin")) {
            String errorMessage = "Path must end with '/bin' for type: " + systemVersionDto.getType();
            log.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        try {
            // 2. Check version command based on type
            String command;
            if ("JAVA".equalsIgnoreCase(systemVersionDto.getType())) {
                command = path.resolve("java") + " -version";
            } else if ("MAVEN".equalsIgnoreCase(systemVersionDto.getType())) {
                command = path.resolve("mvn") + " -version";
            } else {
                throw new IllegalArgumentException("Unsupported system version type: " + systemVersionDto.getType());
            }

            log.debug("Executing validation command: {}", command);
            Process process = Runtime.getRuntime().exec(command);
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                String errorMessage = "Failed to execute version command for " + systemVersionDto.getType() +
                        ". Exit code: " + exitCode;
                log.error(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }

            log.info("Validation successful for system version: {}", systemVersionDto.getName());
        } catch (IOException e) {
            log.error("IO error during version validation: {}", e.getMessage());
            throw new IllegalArgumentException("Failed to execute version command: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Version validation interrupted: {}", e.getMessage());
            throw new IllegalArgumentException("Version validation interrupted", e);
        }
    }
}