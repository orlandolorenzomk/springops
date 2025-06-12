package org.kreyzon.springops.core.deployment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kreyzon.springops.common.dto.deployment.*;
import org.kreyzon.springops.common.dto.application_env.ApplicationEnvDto;
import org.kreyzon.springops.common.enums.DeploymentStatus;
import org.kreyzon.springops.common.enums.DeploymentType;
import org.kreyzon.springops.common.exception.SpringOpsException;
import org.kreyzon.springops.common.utils.DeploymentUtils;
import org.kreyzon.springops.common.utils.EncryptionUtils;
import org.kreyzon.springops.common.utils.PortUtils;
import org.kreyzon.springops.config.ApplicationConfig;
import org.kreyzon.springops.core.application.entity.Application;
import org.kreyzon.springops.core.application.service.ApplicationLookupService;
import org.kreyzon.springops.core.application_env.service.ApplicationEnvService;
import org.kreyzon.springops.core.deployment.entity.Deployment;
import org.kreyzon.springops.setup.domain.Setup;
import org.kreyzon.springops.setup.service.SetupService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Service class for managing deployments of applications.
 * This service handles the deployment process, including updating the project,
 * building the project, and running the application.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeploymentManagerService {

    private final ApplicationLookupService applicationLookupService;
    private final ApplicationConfig applicationConfig;
    private final SetupService setupService;
    private final ApplicationEnvService applicationEnvService;
    private final DeploymentService deploymentService;

    /**
     * Retrieves the deployment status of the latest deployment for a given application.
     *
     * @param applicationId the ID of the application to check
     * @return a DeploymentStatusDto containing the status and port information
     */
    public DeploymentStatusDto getDeploymentStatus(Integer applicationId) {
        log.info("Retrieving deployment status for application ID: {}", applicationId);

        // Check if the application exists
        applicationLookupService.findEntityById(applicationId);

        DeploymentStatusDto statusDto = new DeploymentStatusDto();

        List<Deployment> deployments = deploymentService.findByApplicationId(applicationId);
        if (deployments.isEmpty()) {
            log.info("No deployments found for application ID: {}", applicationId);
            statusDto.setIsRunning(false);
            statusDto.setPort("");
            statusDto.setPid("");
            return statusDto;
        }

        Deployment latestDeployment = deployments.get(deployments.size() - 1);

        Integer pid = latestDeployment.getPid();
        boolean isRunning = latestDeployment.getStatus().equals(DeploymentStatus.RUNNING)
                && DeploymentUtils.isPidRunning(pid);

        statusDto.setIsRunning(isRunning);
        if (isRunning) {
            String ports = DeploymentUtils.getListeningPorts(pid);
            statusDto.setPort(ports);
            statusDto.setPid(pid.toString());
        } else {
            statusDto.setPort("");
        }
        return statusDto;
    }

    /**
     * Kills the deployment process with the given PID.
     *
     * @param pid the process ID of the deployment to kill
     * @return true if the process was successfully killed, false otherwise
     */
    public boolean killDeploymentProcess(Integer pid) {
        if (pid == null) {
            log.warn("PID is null, nothing to kill.");
            return false;
        }
        if (!DeploymentUtils.isPidRunning(pid)) {
            log.info("Process with PID {} is not running.", pid);
            return false;
        }

        try {
            Process killProcess = new ProcessBuilder("kill", "-9", pid.toString()).start();
            int exitCode = killProcess.waitFor();
            if (exitCode == 0) {
                log.info("Successfully killed process with PID {}", pid);
                Deployment deployment = deploymentService.findByPid(pid);
                deployment.setStatus(DeploymentStatus.STOPPED);
                deploymentService.update(DeploymentDto.fromEntity(deployment));
                return true;
            } else {
                log.error("Failed to kill process with PID {}, exit code: {}", pid, exitCode);
                return false;
            }
        } catch (Exception e) {
            log.error("Exception while killing process with PID {}: {}", pid, e.getMessage());
            return false;
        }
    }

    /**
     * Manages the deployment of an application by updating the project, building it, and running it.
     *
     * @param applicationId the ID of the application to deploy
     * @param branchName    the name of the branch to deploy
     * @throws SpringOpsException if the deployment process fails due to:
     *  *         - The application is already running ({@link HttpStatus#CONFLICT}).
     *  *         - Missing Maven or Java system versions ({@link HttpStatus#BAD_REQUEST}).
     *  *         - Missing Git token configuration ({@link HttpStatus#BAD_REQUEST}).
     *  *         - Error decrypting environment variables ({@link HttpStatus#INTERNAL_SERVER_ERROR}).
     * @return a DeploymentResultDto containing the results of the deployment process
     */
    @Transactional
    public DeploymentResultDto manageDeployment(Integer applicationId, String branchName, Integer port) {
        Application application = validateAndPrepareDeployment(applicationId);
        logDeploymentStart(application, branchName);

        Integer portForDeployment = port != null ? port : application.getPort();

        if (applicationLookupService.isPortAlreadyInUseByOtherApplications(portForDeployment)) {
            log.warn("Another deployment is already running on port {}", portForDeployment);
            throw new SpringOpsException("Another deployment is already running on this port", HttpStatus.CONFLICT);
        }

        if (PortUtils.isPortOccupied(portForDeployment)) {
            log.error("Invalid or occupied port: {}", portForDeployment);
            throw new SpringOpsException("Invalid or occupied port for deployment", HttpStatus.BAD_REQUEST);
        }

        DeploymentResultDto deploymentResult = new DeploymentResultDto();
        try {
            DeploymentContextDto context = prepareDeploymentContext(application, branchName, portForDeployment);
            executeDeploymentSteps(application, context, deploymentResult);
            handleSuccessfulDeployment(applicationId, deploymentResult);
        } catch (SpringOpsException e) {
            throw e; // Re-throw known exceptions
        } catch (Exception e) {
            handleDeploymentFailure(applicationId, deploymentResult, e);
        }
        return deploymentResult;
    }

    private Application validateAndPrepareDeployment(Integer applicationId) {
        Application application = applicationLookupService.findEntityById(applicationId);

        if (getDeploymentStatus(applicationId).getIsRunning()) {
            log.warn("Application ID {} is already running. Deployment aborted.", applicationId);
            throw new SpringOpsException("Application is already running. Please stop it before redeploying.", HttpStatus.CONFLICT);
        }

        validateSystemVersions(application);
        return application;
    }

    private void validateSystemVersions(Application application) {
        if (application.getMvnSystemVersion() == null) {
            log.error("Maven system version is not set for application ID {}", application.getId());
            throw new SpringOpsException("Maven system version is not set", HttpStatus.BAD_REQUEST);
        }
        if (application.getJavaSystemVersion() == null) {
            log.error("Java system version is not set for application ID {}", application.getId());
            throw new SpringOpsException("Java system version is not set", HttpStatus.BAD_REQUEST);
        }
    }

    private void logDeploymentStart(Application application, String branchName) {
        log.info("""
            #############################################################
            #                                                           #
            #   STARTING DEPLOYMENT FOR APPLICATION: {}                #
            #   ON BRANCH: {}                                          #
            #                                                           #
            #############################################################
            """, application.getName(), branchName);
    }

    private DeploymentContextDto prepareDeploymentContext(Application application, String branchName, Integer port) {
        log.info("Preparing deployment context for application ID: {}, branch: {}", application.getId(), branchName);
        Setup setup = setupService.getSetup();
        String gitToken = validateAndGetGitToken();

        String repositoryUrl = application.getGitProjectHttpsUrl();
        String authenticatedUrl = repositoryUrl.replace("https://", "https://" + gitToken + "@");

        String sourcePath = Paths.get(
                setup.getFilesRoot(),
                applicationConfig.getRootDirectoryName(),
                applicationConfig.getDirectoryApplications(),
                application.getName().trim().toLowerCase().replaceAll("\\s+", "-"),
                applicationConfig.getDirectorySource()
        ).toString();

        return new DeploymentContextDto(
                authenticatedUrl,
                sourcePath,
                application.getJavaSystemVersion(),
                application.getMvnSystemVersion(),
                branchName,
                prepareEnvironmentVariables(application.getId()),
                port
        );
    }

    private String validateAndGetGitToken() {
        log.info("Validating Git token configuration");
        String gitToken = applicationConfig.getGitToken();
        if (gitToken == null || gitToken.isEmpty()) {
            throw new SpringOpsException("Git token is not configured", HttpStatus.BAD_REQUEST);
        }
        return gitToken;
    }

    private String prepareEnvironmentVariables(Integer applicationId) {
        log.info("Preparing environment variables for application ID: {}", applicationId);
        List<ApplicationEnvDto> envs = applicationEnvService.findByApplicationId(applicationId);
        return envs.stream()
                .map(this::decryptEnvVariable)
                .collect(Collectors.joining(" "));
    }

    private String decryptEnvVariable(ApplicationEnvDto env) {
        try {
            log.info("Decrypting environment variable: {}", env.getName());
            return env.getName() + "=" + (env.getValue() != null ? EncryptionUtils.decrypt(env.getValue(), applicationConfig.getSecret(), applicationConfig.getAlgorithm()) : "");
        } catch (Exception e) {
            log.error("Error decrypting environment variable {}: {}", env.getName(), e.getMessage());
            throw new SpringOpsException("Failed to decrypt environment variable " + env.getName(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void executeDeploymentSteps(Application application, DeploymentContextDto context, DeploymentResultDto result) throws Exception {
        log.info("Executing deployment steps for application ID: {}", application.getId());
        ObjectMapper objectMapper = new ObjectMapper();

        // Step 1: Update project
        CommandResultDto updateResult = executeCommand("update_project.sh",
                context.authenticatedUrl(), context.branchName(), context.sourcePath());
        result.setUpdateResult(updateResult);

        if (updateResult.getExitCode() != 0) {
            result.setSuccess(false);
            return;
        }

        JsonNode updateJsonNode = objectMapper.readTree(updateResult.getOutput());
        if (!updateJsonNode.get("success").asBoolean()) {
            result.setSuccess(false);
            log.error("Update failed for application ID {}: {}", application.getId(), updateJsonNode.get("message").asText());
            return;
        }

        String deployBranch = updateJsonNode.path("deployBranch").asText();
        if (deployBranch.isEmpty()) {
            result.setSuccess(false);
            return;
        }

        // Step 2: Build project
        CommandResultDto buildResult = captureCommandOutput("build_project.sh",
                context.javaVersion().getPath(),
                context.mavenVersion().getPath(),
                context.sourcePath(),
                context.javaVersion().getVersion());
        result.setBuildResult(buildResult);

        if (buildResult.getExitCode() != 0) {
            result.setSuccess(false);
            log.error("Build failed for application ID {}: {}", application.getId(), buildResult.getOutput());
            return;
        }

        JsonNode buildJsonNode = objectMapper.readTree(buildResult.getOutput());
        if (!buildJsonNode.get("success").asBoolean()) {
            result.setSuccess(false);
            return;
        }

        JsonNode artifactsNode = buildJsonNode.get("artifacts");
        if (artifactsNode == null || !artifactsNode.isArray() || artifactsNode.isEmpty()) {
            result.setSuccess(false);
            return;
        }

        String jarName = artifactsNode.get(0).asText();
        result.setBuiltJar(jarName);

        // Step 3: Run project
        CommandResultDto runResult = executeCommand("run_project.sh",
                context.javaVersion().getPath(),
                context.sourcePath(),
                jarName,
                context.port().toString(),
                context.environmentVariables());
        result.setRunResult(runResult);
        result.setSuccess(runResult.getExitCode() == 0);
    }

    private void handleSuccessfulDeployment(Integer applicationId, DeploymentResultDto result) {
        if (result.isSuccess()) {
            log.info("Deployment for application ID {} completed successfully", applicationId);
            updateDeploymentRecords(applicationId, result);
        } else {
            log.error("Deployment for application ID {} failed with exit code: {}",
                    applicationId, result.getRunResult().getExitCode());
        }
    }

    private void updateDeploymentRecords(Integer applicationId, DeploymentResultDto result) {
        Deployment latestDeployment = deploymentService.findLatestByApplicationId(applicationId);
        if (latestDeployment != null) {
            latestDeployment.setType(DeploymentType.PREVIOUS);
            latestDeployment.setStatus(DeploymentStatus.STOPPED);
            deploymentService.update(DeploymentDto.fromEntity(latestDeployment));
        }

        DeploymentDto newDeployment = DeploymentDto.builder()
                .version(result.getBuiltJar())
                .status(DeploymentStatus.RUNNING)
                .type(DeploymentType.LATEST)
                .createdAt(Instant.now())
                .applicationId(applicationId)
                .pid(Integer.valueOf(result.getRunResult().getOutput().trim()))
                .branch(result.getUpdateResult() != null ?
                        extractDeployBranch(result.getUpdateResult().getOutput()) : "unknown")
                .build();
        deploymentService.save(newDeployment);
    }

    private String extractDeployBranch(String updateOutput) {
        try {
            JsonNode node = new ObjectMapper().readTree(updateOutput);
            return node.path("deployBranch").asText();
        } catch (Exception e) {
            log.warn("Failed to extract deploy branch from update output", e);
            return "unknown";
        }
    }

    private void handleDeploymentFailure(Integer applicationId, DeploymentResultDto result, Exception e) {
        log.error("Error managing deployment for application ID {}: {}", applicationId, e.getMessage());
        result.setSuccess(false);
        result.setUpdateResult(new CommandResultDto(-1, e.toString()));
    }

    /**
     * Executes a shell command to run a script with the provided arguments.
     *
     * @param scriptName the name of the script to execute
     * @param args       the arguments to pass to the script
     * @return a CommandResultDto containing the exit code and output of the command
     * @throws IOException          if an I/O error occurs
     * @throws InterruptedException if the process is interrupted
     */
    private CommandResultDto executeCommand(String scriptName, String... args) throws IOException, InterruptedException {
        log.info("Executing script: {} with {} arguments", scriptName, args.length);
        String scriptPath = Objects.requireNonNull(getClass().getClassLoader().getResource("scripts/" + scriptName)).getPath();

        // Log the entire command being executed
        log.info("Command to be executed: /bin/bash {} {}", scriptPath, String.join(" ", args));

        String command = String.format("/bin/bash %s %s", scriptPath, String.join(" ", args));
        ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", command);
        Process process = processBuilder.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
            }
        }

        if (applicationConfig.getDisplayProcessLogs()) {
            log.info("Output from script {}: {}", scriptName, output);
        } else {
            log.debug("Output from script {}: {}", scriptName, output);
        }

        int exitCode = process.waitFor();
        log.info("Script {} executed with exit code: {}", scriptName, exitCode);
        return new CommandResultDto(exitCode, output.toString());
    }

    /**
     * Captures the output of a command executed by a script.
     *
     * @param scriptName the name of the script to execute
     * @param args       the arguments to pass to the script
     * @return a CommandResultDto containing the exit code and output of the command
     * @throws IOException          if an I/O error occurs
     * @throws InterruptedException if the process is interrupted
     */
    private CommandResultDto captureCommandOutput(String scriptName, String... args) throws IOException, InterruptedException {
        return executeCommand(scriptName, args);
    }
}
