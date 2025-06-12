package org.kreyzon.springops.core.logs.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kreyzon.springops.common.dto.logs.ApplicationLogDto;
import org.kreyzon.springops.common.exception.SpringOpsException;
import org.kreyzon.springops.config.ApplicationConfig;
import org.kreyzon.springops.core.application.entity.Application;
import org.kreyzon.springops.core.application.service.ApplicationLookupService;
import org.kreyzon.springops.setup.domain.Setup;
import org.kreyzon.springops.setup.service.SetupService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for streaming logs of different applications.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LogService {

    private final ApplicationConfig applicationConfig;

    private final ApplicationLookupService applicationLookupService;

    private final SetupService setupService;

    /**
     * Lists all application logs for the specified application.
     *
     * @param applicationId the ID of the application whose logs are to be listed
     * @return a list of {@link ApplicationLogDto} containing the log file names
     */
    public List<ApplicationLogDto> listAllApplicationLogs(Integer applicationId) {
        log.info("Listing all logs for application with ID: {}", applicationId);

        Application application = applicationLookupService.findEntityById(applicationId);
        Setup setup = setupService.getSetup();

        String logsPath = setup.getFilesRoot() + "/" +
                applicationConfig.getRootDirectoryName() + "/" +
                applicationConfig.getDirectoryApplications() + "/" +
                application.getName().toLowerCase().replace(" ", "-") + "/logs";

        try (var pathsStream = Files.list(Paths.get(logsPath))) {
            List<Path> allLogs = pathsStream
                    .filter(Files::isRegularFile)
                    .filter(file -> file.getFileName().toString().endsWith(".log"))
                    .toList();

            List<ApplicationLogDto> result = new ArrayList<>();

            // Add latest.log first if it exists
            allLogs.stream()
                    .filter(file -> file.getFileName().toString().equals("latest.log"))
                    .findFirst()
                    .ifPresent(file -> result.add(new ApplicationLogDto(file.getFileName().toString())));

            // Add the rest, excluding latest.log, sorted by last modified descending
            allLogs.stream()
                    .filter(file -> !file.getFileName().toString().equals("latest.log"))
                    .sorted(Comparator.comparingLong(file -> ((Path) file).toFile().lastModified()).reversed())
                    .map(file -> new ApplicationLogDto(file.getFileName().toString()))
                    .forEach(result::add);

            return result;
        } catch (IOException e) {
            log.error("Failed to list application logs from path: {}", logsPath, e);
            throw new SpringOpsException("Error listing application logs", HttpStatus.BAD_REQUEST);
        }
    }


    /**
     * Downloads a specific log file for the given application.
     *
     * @param applicationId the ID of the application whose log file is to be downloaded
     * @param filename      the name of the log file to be downloaded
     * @return byte array containing the contents of the log file
     */
    public byte[] downloadLogFile(Integer applicationId, String filename) {
        log.info("Downloading log file '{}' for application with ID: {}", filename, applicationId);

        Application application = applicationLookupService.findEntityById(applicationId);
        Setup setup = setupService.getSetup();

        String logFilePath = setup.getFilesRoot() + "/" +
                applicationConfig.getRootDirectoryName() + "/" +
                applicationConfig.getDirectoryApplications() + "/" +
                application.getName().toLowerCase().replace(" ", "-") + "/logs/" + filename;

        Path path = Paths.get(logFilePath);

        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            log.error("Log file '{}' not found at path: {}", filename, logFilePath);
            throw new SpringOpsException("Log file not found", HttpStatus.NOT_FOUND);
        }

        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            log.error("Failed to read log file: {}", logFilePath, e);
            throw new RuntimeException("Error reading log file", e);
        }
    }

}
