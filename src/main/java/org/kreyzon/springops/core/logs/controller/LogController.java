package org.kreyzon.springops.core.logs.controller;

import lombok.RequiredArgsConstructor;
import org.kreyzon.springops.common.dto.logs.ApplicationLogDto;
import org.kreyzon.springops.config.ApplicationConfig;
import org.kreyzon.springops.core.application.service.ApplicationLookupService;
import org.kreyzon.springops.core.logs.service.LogService;
import org.kreyzon.springops.setup.service.SetupService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller class for managing application logs.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/logs")
public class LogController {

    private final LogService logService;

    private final ApplicationConfig applicationConfig;

    private final ApplicationLookupService applicationLookupService;

    private final SetupService setupService;

    /**
     * Endpoint to list all logs for a specific application.
     *
     * @param applicationId the ID of the application whose logs are to be listed
     * @return a list of {@link ApplicationLogDto} containing the log file names
     */
    @GetMapping("/list")
    public ResponseEntity<List<ApplicationLogDto>> listApplicationLogs(@RequestParam Integer applicationId) {
        return ResponseEntity.ok(logService.listAllApplicationLogs(applicationId));
    }

    /**
     * Endpoint to stream logs for a specific application.
     *
     * @param applicationId the ID of the application whose logs are to be streamed
     * @return a ResponseBodyEmitter that streams the log content
     */
    @GetMapping("/{applicationId}/download")
    public ResponseEntity<byte[]> downloadLogFile(
            @PathVariable Integer applicationId,
            @RequestParam String filename) {

        byte[] fileContent = logService.downloadLogFile(applicationId, filename);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(fileContent);
    }
}