package org.kreyzon.springops.core.system_version.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kreyzon.springops.common.dto.system_version.SystemVersionDto;
import org.kreyzon.springops.common.exception.SpringOpsException;
import org.kreyzon.springops.config.annotations.Audit;
import org.kreyzon.springops.core.system_version.entity.SystemVersion;
import org.kreyzon.springops.core.system_version.repository.SystemVersionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
     * @throws SpringOpsException with {@link HttpStatus#NOT_FOUND} if no system version is found with the given ID
     */
    public SystemVersionDto findById(Integer id) {
        log.info("Fetching system version with ID: {}", id);
        return systemVersionRepository.findById(id)
                .map(SystemVersionDto::fromEntity)
                .orElseThrow(() -> new SpringOpsException("System version not found with ID: " + id, HttpStatus.NOT_FOUND));
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
     * @throws SpringOpsException with {@link HttpStatus#CONFLICT} if a system version with the same name already exists
     * @return the saved {@link SystemVersionDto}
     */
    @Audit
    public SystemVersionDto save(SystemVersionDto systemVersionDto) {
        log.info("Saving system version: {}", systemVersionDto);

        validateSystemVersion(systemVersionDto);

        if (systemVersionRepository.existsByName(systemVersionDto.getName())) {
            log.warn("System version with name '{}' already exists", systemVersionDto.getName());
            throw new SpringOpsException("A system version with name '" + systemVersionDto.getName() + "' already exists.", HttpStatus.CONFLICT);
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
     * @throws SpringOpsException with {@link HttpStatus#NOT_FOUND} if no system version is found with the given ID
     * @throws SpringOpsException with {@link HttpStatus#CONFLICT} if another system version with the same name exists
     */
    @Audit
    public SystemVersionDto update(Integer id, SystemVersionDto systemVersionDto) {
        log.info("Updating system version with ID: {}", id);
        SystemVersion existingEntity = systemVersionRepository.findById(id)
                .orElseThrow(() -> new SpringOpsException("System version not found with ID: " + id, HttpStatus.NOT_FOUND));

        validateSystemVersion(systemVersionDto);

        if (systemVersionRepository.existsByName(systemVersionDto.getName()) &&
                !existingEntity.getName().equals(systemVersionDto.getName())) {
            log.warn("System version with name '{}' already exists", systemVersionDto.getName());
            throw new SpringOpsException("Another system version with name '" + systemVersionDto.getName() + "' already exists.", HttpStatus.CONFLICT);
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
     * @throws SpringOpsException with {@link HttpStatus#NOT_FOUND} if no system version is found with the given ID
     */
    @Audit
    public void delete(Integer id) {
        log.info("Deleting system version with ID: {}", id);
        if (!systemVersionRepository.existsById(id)) {
            throw new SpringOpsException("System version not found with ID: " + id, HttpStatus.NOT_FOUND);
        }
        systemVersionRepository.deleteById(id);
        log.info("System version with ID: {} deleted successfully", id);
    }

    /**
     * Finds a system version by its type.
     *
     * @param type the type of the system version to find
     * @return the corresponding {@link SystemVersion}
     * @throws SpringOpsException with {@link HttpStatus#NOT_FOUND} if no system version is found with the given type
     */
    public SystemVersion findByType(String type) {
        log.info("Finding system version by type: {}", type);
        return systemVersionRepository.findByType(type)
                .orElseThrow(() -> new SpringOpsException("System version not found with type: " + type, HttpStatus.NOT_FOUND));
    }

    /**
     * Validates a system version by checking:
     * 1. If the path exists
     * 2. If the version command executes properly
     *
     * @param systemVersionDto the system version to validate
     *  @throws SpringOpsException if:
     *  *         - The path does not exist ({@link HttpStatus#BAD_REQUEST}).
     *  *         - The path does not end with '/bin' for JAVA or MAVEN types ({@link HttpStatus#BAD_REQUEST}).
     *  *         - The system version type is unsupported ({@link HttpStatus#BAD_REQUEST}).
     *  *         - The version command fails ({@link HttpStatus#BAD_REQUEST}).
     *  *         - An I/O error occurs ({@link HttpStatus#INTERNAL_SERVER_ERROR}).
     *  *         - The validation process is interrupted ({@link HttpStatus#INTERNAL_SERVER_ERROR}).
     */
    public void validateSystemVersion(SystemVersionDto systemVersionDto) {
        log.info("Validating system version: {}", systemVersionDto.getName());

        // 1. Check if path exists
        Path path = Paths.get(systemVersionDto.getPath().trim());
        if (!Files.exists(path)) {
            log.error("Path does not exist: {}", systemVersionDto.getPath());
            throw new SpringOpsException("Path does not exist: " + systemVersionDto.getPath(), HttpStatus.BAD_REQUEST);
        }

        // 1.5. Validate path ends with /bin for JAVA and MAVEN
        String normalizedPath = path.toString().replace("\\", "/"); // for Linux compatibility
        if (("JAVA".equalsIgnoreCase(systemVersionDto.getType()) ||
                "MAVEN".equalsIgnoreCase(systemVersionDto.getType())) &&
                !normalizedPath.endsWith("/bin")) {
            String errorMessage = "Path must end with '/bin' for type: " + systemVersionDto.getType();
            log.error(errorMessage);
            throw new SpringOpsException(errorMessage, HttpStatus.BAD_REQUEST);
        }

        try {
            // 2. Check version command based on type
            String command;
            if ("JAVA".equalsIgnoreCase(systemVersionDto.getType())) {
                command = normalizedPath + "/java -version";
            } else if ("MAVEN".equalsIgnoreCase(systemVersionDto.getType())) {
                command = normalizedPath + "/mvn -version";
            } else {
                throw new SpringOpsException("Unsupported system version type: " + systemVersionDto.getType(), HttpStatus.BAD_REQUEST);
            }

            log.debug("Executing validation command: {}", command);
            Process process = new ProcessBuilder()
                    .command("bash", "-c", command) // Use bash for Linux shell execution
                    .start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                 BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
                while ((line = errorReader.readLine()) != null) {
                    output.append(line).append("\n");
                }

                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    String errorMessage = "Failed to execute version command for " + systemVersionDto.getType() +
                            ". Exit code: " + exitCode;
                    log.error(errorMessage);
                    throw new SpringOpsException(errorMessage, HttpStatus.BAD_REQUEST);
                }

                log.debug("Command output: {}", output);

                // 3. Parse the output to extract the version
                String extractedVersion = extractVersionFromOutput(output.toString(), systemVersionDto.getType());
                if (!extractedVersion.startsWith(systemVersionDto.getVersion())) { // We use contains because versions can be with minor updates
                    String errorMessage = "Version mismatch. Expected: " + systemVersionDto.getVersion() +
                            ", Found: " + extractedVersion;
                    log.error(errorMessage);
                    throw new SpringOpsException(errorMessage, HttpStatus.BAD_REQUEST);
                }

                log.info("Validation successful for system version: {}", systemVersionDto.getName());
            }
        } catch (IOException e) {
            log.error("IO error during version validation: {}", e.getMessage());
            throw new SpringOpsException("Failed to execute version command: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Version validation interrupted: {}", e.getMessage());
            throw new SpringOpsException("Version validation interrupted" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Extracts the version from the command output based on the type.
     *
     * @param output the command output
     * @param type the system version type (e.g., JAVA or MAVEN)
     * @return the extracted version
     */
    private String extractVersionFromOutput(String output, String type) {
        if ("JAVA".equalsIgnoreCase(type)) {
            // Example output: 'java version "17.0.1"'
            return output.lines()
                    .filter(line -> line.contains("version"))
                    .findFirst()
                    .map(line -> line.split("\"")[1]) // Extract version between quotes
                    .orElseThrow(() -> new SpringOpsException("Failed to parse Java version from output", HttpStatus.BAD_REQUEST));
        } else if ("MAVEN".equalsIgnoreCase(type)) {
            // Example output: 'Apache Maven 3.8.4'
            return output.lines()
                    .filter(line -> line.startsWith("Apache Maven"))
                    .findFirst()
                    .map(line -> line.split(" ")[2]) // Extract version after 'Apache Maven'
                    .orElseThrow(() -> new SpringOpsException("Failed to parse Maven version from output", HttpStatus.BAD_REQUEST));
        } else {
            throw new SpringOpsException("Unsupported system version type: " + type, HttpStatus.BAD_REQUEST);
        }
    }
}