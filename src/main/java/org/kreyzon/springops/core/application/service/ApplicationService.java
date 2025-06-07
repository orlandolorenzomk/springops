package org.kreyzon.springops.core.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.kreyzon.springops.common.dto.application.ApplicationDto;
import org.kreyzon.springops.common.dto.application.ApplicationRunDto;
import org.kreyzon.springops.common.dto.system_version.SystemVersionDto;
import org.kreyzon.springops.config.ApplicationConfig;
import org.kreyzon.springops.core.application.entity.Application;
import org.kreyzon.springops.core.application.repository.ApplicationRepository;
import org.kreyzon.springops.core.system_version.entity.SystemVersion;
import org.kreyzon.springops.core.system_version.service.SystemVersionService;
import org.kreyzon.springops.setup.service.SetupService;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Represents the service layer for managing Application entities.
 * This service provides methods to interact with the Application repository
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationService {

    private final ApplicationRepository applicationRepository;

    private final SystemVersionService systemVersionService;

    private final ApplicationConfig applicationConfig;

    private final SetupService setupService;

    /**
     * Finds an Application by its ID.
     *
     * @param id the ID of the Application to find
     * @return an ApplicationDto representing the found Application, or null if not found
     */
    public ApplicationDto findById(Integer id) {
        return applicationRepository.findById(id)
                .map(ApplicationDto::fromEntity)
                .orElseThrow(() -> new IllegalArgumentException("Application with ID '" + id + "' does not exist"));
    }

    /**
     * Finds all Applications.
     *
     * @return a list of ApplicationDto representing all Applications
     */
    public List<ApplicationDto> findAll() {
        return applicationRepository.findAll().stream()
                .map(ApplicationDto::fromEntity)
                .toList();
    }

    /**
     * Saves a new Application.
     *
     * @param applicationDto the ApplicationDto to save
     * @return the saved ApplicationDto
     */
    public ApplicationDto save(ApplicationDto applicationDto) {
        SystemVersionDto mvnSystemVersion = systemVersionService.findById(applicationDto.getMvnSystemVersionId());
        SystemVersion systemVersion = SystemVersionDto.toEntity(mvnSystemVersion);
        SystemVersionDto javaSystemVersion = systemVersionService.findById(applicationDto.getJavaSystemVersionId());
        SystemVersion javaVersion = SystemVersionDto.toEntity(javaSystemVersion);

        if (applicationRepository.existsByName(applicationDto.getName())) {
            log.warn("Application with name '{}' already exists", applicationDto.getName());
            throw new IllegalArgumentException("Application with name '" + applicationDto.getName() + "' already exists");
        }
        Application application = ApplicationDto.toEntity(applicationDto);
        application.setMvnSystemVersion(systemVersion);
        application.setJavaSystemVersion(javaVersion);
        application.setCreatedAt(java.time.Instant.now());
        application.setFolderRoot(applicationDto.getName().trim().toLowerCase(Locale.ROOT));

        generateApplicationFolders(application.getName());

        Application savedApplication = applicationRepository.save(application);
        return ApplicationDto.fromEntity(savedApplication);
    }

    /**
     * Updates an existing Application.
     *
     * @param id the ID of the Application to update
     * @param applicationDto the ApplicationDto containing updated data
     *
     * @return the updated ApplicationDto
     */
    public ApplicationDto update(Integer id, ApplicationDto applicationDto) {
        SystemVersionDto mvnSystemVersion = systemVersionService.findById(applicationDto.getMvnSystemVersionId());
        SystemVersion systemVersion = SystemVersionDto.toEntity(mvnSystemVersion);
        SystemVersionDto javaSystemVersion = systemVersionService.findById(applicationDto.getJavaSystemVersionId());
        SystemVersion javaVersion = SystemVersionDto.toEntity(javaSystemVersion);

        if (applicationRepository.existsByName(applicationDto.getName())) {
            log.warn("Application with name '{}' already exists", applicationDto.getName());
            throw new IllegalArgumentException("Application with name '" + applicationDto.getName() + "' already exists");
        }

        if (!applicationRepository.existsById(id)) {
            log.warn("Application with ID '{}' does not exist", id);
            throw new IllegalArgumentException("Application with ID '" + id + "' does not exist");
        }
        if (applicationRepository.existsByName(applicationDto.getName()) && !applicationDto.getId().equals(id)) {
            log.warn("Application with name '{}' already exists", applicationDto.getName());
            throw new IllegalArgumentException("Application with name '" + applicationDto.getName() + "' already exists");
        }
        Application application = ApplicationDto.toEntity(applicationDto);
        application.setId(id);
        application.setMvnSystemVersion(systemVersion);
        application.setJavaSystemVersion(javaVersion);
        Application updatedApplication = applicationRepository.save(application);
        return ApplicationDto.fromEntity(updatedApplication);
    }

    /**
     * Deletes an Application by its ID.
     *
     * @param id the ID of the Application to delete
     */
    public void deleteById(Integer id) {
        if (!applicationRepository.existsById(id)) {
            log.warn("Application with ID '{}' does not exist", id);
            throw new IllegalArgumentException("Application with ID '" + id + "' does not exist");
        }
        applicationRepository.deleteById(id);
        log.info("Application with ID '{}' deleted successfully", id);
    }

    /**
     * Generates application folders including root, source, and backup directories.
     * Ensures the directories are created if they do not already exist.
     *
     * @param applicationName the name of the application for which folders are generated
     */
    private void generateApplicationFolders(String applicationName) {
        log.info("Generating application folders in: {}", applicationConfig.getRootDirectoryName() + "/" + applicationConfig.getDirectoryApplications());
        applicationName = applicationName.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", "-");

        String rootDirectory = setupService.getSetup().getFilesRoot() + "/" +
                applicationConfig.getRootDirectoryName() + "/" +
                applicationConfig.getDirectoryApplications() + "/" +
                applicationName;

        createDirectory(rootDirectory);

        String sourceDirectory = rootDirectory + "/" + applicationConfig.getDirectorySource();
        String backupDirectory = rootDirectory + "/" + applicationConfig.getDirectoryBackups();

        createDirectory(sourceDirectory);
        createDirectory(backupDirectory);
    }

    /**
     * Creates a directory if it does not exist and logs the result.
     *
     * @param directoryPath the path of the directory to create
     */
    private void createDirectory(String directoryPath) {
        java.io.File directory = new java.io.File(directoryPath);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
                log.info("Created directory: {}", directoryPath);
            } else {
                log.error("Failed to create  directory: {}", directoryPath);
            }
        } else {
            log.info("Directory already exists: {}", directoryPath);
        }
    }

    /**
     * Pulls the application repository from GitLab, builds it using Maven, and runs the project using Java.
     *
     * @param applicationId the ID of the application to pull and build
     * @param branchName the branch name to pull from GitLab
     * @return ApplicationRunDto containing details of the process
     */
    public ApplicationRunDto pullBuildAndRunProject(Integer applicationId, String branchName) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application with ID '" + applicationId + "' does not exist"));

        SystemVersionDto mavenVersion = systemVersionService.findById(application.getMvnSystemVersion().getId());
        SystemVersionDto javaVersion = systemVersionService.findById(application.getJavaSystemVersion().getId());

        if (mavenVersion == null || javaVersion == null) {
            throw new IllegalArgumentException("Both Maven and Java system versions must be configured");
        }

        String mavenPath = mavenVersion.getPath();
        String javaPath = javaVersion.getPath();

        String gitUrl = application.getGitProjectHttpsUrl();
        if (gitUrl == null || gitUrl.isEmpty()) {
            throw new IllegalArgumentException("Git URL is not configured for the application");
        }

        String gitToken = applicationConfig.getGitToken();
        if (gitToken == null || gitToken.isEmpty()) {
            throw new IllegalArgumentException("Git token is not configured");
        }
        gitUrl = gitUrl.replace("https://", "https://" + gitToken + "@");

        String cloneDirectory = setupService.getSetup().getFilesRoot() + "/" +
                applicationConfig.getDirectoryApplications() + "/" +
                application.getName().toLowerCase(Locale.ROOT).replaceAll("\\s+", "-") + "/" +
                applicationConfig.getDirectorySource();

        ApplicationRunDto runDto = ApplicationRunDto.builder()
                .applicationName(application.getName())
                .branchName(branchName)
                .mavenPath(mavenPath)
                .javaPath(javaPath)
                .build();

        try {
            log.info("Cloning repository for application '{}' from branch '{}'", application.getName(), branchName);
            File cloneDir = new File(cloneDirectory);
            if (cloneDir.exists()) {
                FileUtils.cleanDirectory(cloneDir);
            } else {
                if (!cloneDir.mkdirs()) {
                    throw new RuntimeException("Failed to create clone directory: " + cloneDirectory);
                }
            }

            List<String> cloneCommand = Arrays.asList(
                    "git", "clone", "--verbose", "-b", branchName,
                    "--single-branch",
                    gitUrl, "."
            );
            log.info("Running git command: {}", String.join(" ", cloneCommand));
            ProcessBuilder cloneProcessBuilder = new ProcessBuilder(cloneCommand);
            cloneProcessBuilder.directory(cloneDir);
            cloneProcessBuilder.redirectErrorStream(true);

            Process cloneProcess = cloneProcessBuilder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(cloneProcess.getInputStream()))) {
                reader.lines().forEach(line -> log.info("[git] {}", line));
            }

            int cloneExitCode = cloneProcess.waitFor();
            if (cloneExitCode != 0) {
                runDto.setCloneSuccess(false);
                runDto.setCloneMessage("Failed to clone repository from GitHub");
                throw new RuntimeException(runDto.getCloneMessage());
            }
            runDto.setCloneSuccess(true);
            runDto.setCloneMessage("Repository cloned successfully to: " + cloneDirectory);

            log.info("Building the project using Maven...");
            ProcessBuilder buildProcessBuilder = new ProcessBuilder(mavenPath + "/mvn", "clean", "install");
            buildProcessBuilder.directory(cloneDir);
            buildProcessBuilder.inheritIO();
            Process buildProcess = buildProcessBuilder.start();
            int buildExitCode = buildProcess.waitFor();
            if (buildExitCode != 0) {
                runDto.setBuildSuccess(false);
                runDto.setBuildMessage("Failed to build the project using Maven");
                throw new RuntimeException(runDto.getBuildMessage());
            }
            runDto.setBuildSuccess(true);
            runDto.setBuildMessage("Project built successfully");

            log.info("Running the project...");
            ProcessBuilder runProcessBuilder = new ProcessBuilder(javaPath + "/java", "-jar", cloneDirectory + "/target/" + application.getName() + ".jar");
            runProcessBuilder.inheritIO();
            Process runProcess = runProcessBuilder.start();
            runDto.setRunSuccess(true);
            runDto.setRunMessage("Project is running...");
        } catch (Exception e) {
            log.error("Error occurred while pulling, building, or running the project: {}", e.getMessage());
            runDto.setRunSuccess(false);
            runDto.setRunMessage("Error: " + e.getMessage());
        }

        return runDto;
    }
}
