package org.kreyzon.springops.core.deployment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.kreyzon.springops.common.dto.deployment.*;
import org.kreyzon.springops.common.dto.application_env.ApplicationEnvDto;
import org.kreyzon.springops.common.enums.DeploymentStatus;
import org.kreyzon.springops.common.enums.DeploymentType;
import org.kreyzon.springops.common.exception.SpringOpsException;
import org.kreyzon.springops.common.utils.DeploymentUtils;
import org.kreyzon.springops.common.utils.EncryptionUtils;
import org.kreyzon.springops.common.utils.GitUtils;
import org.kreyzon.springops.common.utils.PortUtils;
import org.kreyzon.springops.config.ApplicationConfig;
import org.kreyzon.springops.config.annotations.Audit;
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
import java.util.concurrent.atomic.AtomicReference;
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
    @Audit
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
    @Audit
    @Transactional
    public List<CommandResultDto> manageDeployment(Integer applicationId, String branchName, DeploymentType deploymentType, Integer port) throws GitAPIException {
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

        if (GitUtils.branchExists(application.getGitProjectHttpsUrl(), branchName, applicationConfig.getGitToken())) {
            log.info("Branch {} exists in the repository, proceeding with deployment.", branchName);
        } else {
            log.error("Branch {} does not exist in the repository, aborting deployment.", branchName);
            throw new SpringOpsException("Branch does not exist in the repository", HttpStatus.BAD_REQUEST);
        }

        DeploymentResultDto deploymentResult = new DeploymentResultDto();
        try {
            DeploymentContextDto context = prepareDeploymentContext(application, branchName, deploymentType, portForDeployment);

            List<CommandResultDto> commandResultDtos = executeDeploymentSteps(application, context, deploymentResult);

            AtomicReference<String> status = new AtomicReference<>(DeploymentStatus.SUCCEEDED.name());
            commandResultDtos.forEach(commandResult -> {
                if (commandResult.getExitCode() != 0) {
                    status.set(DeploymentStatus.FAILED.name());
                    log.error("Command failed with exit code {}: {}", commandResult.getExitCode(), commandResult.getOutput());
                }
            });

            String jarName = commandResultDtos.get(1).getData().get(0).toString();
            Integer pid = commandResultDtos.get(2).getData().get(0) != null ? Integer.parseInt(commandResultDtos.get(2).getData().get(1).toString()) : null;
            String branch = commandResultDtos.get(0).getData().get(0) != null ? commandResultDtos.get(0).getData().get(0).toString() : "unknown";
            handleSuccessfulDeployment(applicationId, status.get(), jarName, pid, branch, deploymentType, commandResultDtos);
            return commandResultDtos;
        } catch (SpringOpsException e) {
            throw e; // Re-throw known exceptions
        } catch (Exception e) {
            handleDeploymentFailure(applicationId, deploymentResult, e);
        }
        return List.of();
    }

    /**
     * Validates the application and prepares it for deployment.
     * Checks if the application is already running and validates system versions.
     *
     * @param applicationId the ID of the application to validate
     * @return the validated Application entity
     * @throws SpringOpsException if the application is already running or if system versions are not set
     */
    private Application validateAndPrepareDeployment(Integer applicationId) {
        Application application = applicationLookupService.findEntityById(applicationId);

        if (getDeploymentStatus(applicationId).getIsRunning()) {
            log.warn("Application ID {} is already running. Deployment aborted.", applicationId);
            throw new SpringOpsException("Application is already running. Please stop it before redeploying.", HttpStatus.CONFLICT);
        }

        validateSystemVersions(application);
        return application;
    }

    /**
     * Validates that the application has the required system versions set.
     * Throws an exception if any version is missing.
     *
     * @param application the Application entity to validate
     * @throws SpringOpsException if Maven or Java system versions are not set
     */
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

    /**
     * Logs the start of the deployment process for an application.
     *
     * @param application the Application entity being deployed
     * @param branchName  the name of the branch being deployed
     */
    private void logDeploymentStart(Application application, String branchName) {
        log.info("""
            #############################################################
            #                                                           #
            #   STARTING DEPLOYMENT FOR APPLICATION: {}                 #
            #   ON BRANCH: {}                                           #
            #                                                           #
            #############################################################
            """, application.getName(), branchName);
    }

    /**
     * Prepares the deployment context for the application.
     * This includes setting up the repository URL, source path, environment variables, and port.
     *
     * @param application the Application entity to prepare
     * @param branchName  the name of the branch to deploy
     * @param port        the port to use for deployment
     * @return a DeploymentContextDto containing the prepared context
     */
    private DeploymentContextDto prepareDeploymentContext(Application application, String branchName, DeploymentType deploymentType, Integer port) {
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
                deploymentType,
                prepareEnvironmentVariables(application.getId()),
                port,
                application.getJavaMinimumMemory() != null ? application.getJavaMinimumMemory() : "512m",
                application.getJavaMaximumMemory() != null ? application.getJavaMaximumMemory() : "1024m"
        );
    }

    /**
     * Validates the Git token configuration and retrieves the token.
     *
     * @return the Git token
     * @throws SpringOpsException if the Git token is not configured
     */
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
        String result = envs.stream()
                .map(this::decryptEnvVariable)
                .collect(Collectors.joining(" "));
        return result.isEmpty() ? "" : result;
    }

    /**
     * Decrypts the value of an environment variable.
     *
     * @param env the ApplicationEnvDto containing the environment variable
     * @return the decrypted environment variable in the format "name=value"
     * @throws SpringOpsException if decryption fails
     */
    private String decryptEnvVariable(ApplicationEnvDto env) {
        try {
            log.info("Decrypting environment variable: {}", env.getName());
            log.info("Using secret: {}", applicationConfig.getSecret());
            log.info("Using algorithm: {}", applicationConfig.getAlgorithm());

            String result = env.getName() + "=" +
                    (env.getValue() != null
                            ? EncryptionUtils.decrypt(env.getValue(), applicationConfig.getSecret(), applicationConfig.getAlgorithm())
                            : "");
            log.info("Decrypted environment variable: {}", result);
            return result;
        } catch (Exception e) {
            log.error("Error decrypting environment variable {}: {}", env.getName(), e.getMessage());
            throw new SpringOpsException("Failed to decrypt environment variable " + env.getName(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Executes the deployment steps for the application.
     * This includes updating the project, building it, and running the application.
     *
     * @param application the Application entity being deployed
     * @param context     the DeploymentContextDto containing the deployment context
     * @param result      the DeploymentResultDto to store the results of the deployment
     * @throws Exception if any step in the deployment process fails
     */
    private List<CommandResultDto> executeDeploymentSteps(Application application, DeploymentContextDto context, DeploymentResultDto result) throws Exception {
        log.info("Executing deployment steps for application ID: {}", application.getId());

        CommandResultDto updateResult = updateProject(application, context, result);
        CommandResultDto buildResult = buildProject(application, context, result);
        result.setBuiltJar(buildResult.getData().get(0).toString());
        CommandResultDto runResult = runProject(context, result);

        return List.of(updateResult, buildResult, runResult);
    }

    /**
     * Updates the project by executing the update script.
     *
     * @param application the Application entity being deployed
     * @param context     the DeploymentContextDto containing the deployment context
     * @param result      the DeploymentResultDto to store the results of the deployment
     * @return a CommandResultDto containing the result of the update command
     * @throws IOException          if an I/O error occurs
     * @throws InterruptedException if the process is interrupted
     */
    private CommandResultDto updateProject(Application application, DeploymentContextDto context, DeploymentResultDto result) throws IOException, InterruptedException {
        return executeCommand(context, "update_project.sh",
                context.authenticatedUrl(), context.branchName(), context.sourcePath(), context.deploymentType().name());
    }

    /**
     * Builds the project by executing the build script.
     *
     * @param application the Application entity being deployed
     * @param context     the DeploymentContextDto containing the deployment context
     * @param result      the DeploymentResultDto to store the results of the deployment
     * @return a CommandResultDto containing the result of the build command
     * @throws IOException          if an I/O error occurs
     * @throws InterruptedException if the process is interrupted
     */
    private CommandResultDto buildProject(Application application, DeploymentContextDto context, DeploymentResultDto result) throws IOException, InterruptedException {
        return captureCommandOutput(context,
                "build_project.sh",
                context.javaVersion().getPath(),
                context.mavenVersion().getPath(),
                context.sourcePath(),
                context.javaVersion().getVersion());
    }

    /**
     * Runs the project by executing the run script.
     *
     * @param context the DeploymentContextDto containing the deployment context
     * @param result  the DeploymentResultDto to store the results of the deployment
     * @return a CommandResultDto containing the result of the run command
     * @throws IOException          if an I/O error occurs
     * @throws InterruptedException if the process is interrupted
     */
    private CommandResultDto runProject(DeploymentContextDto context, DeploymentResultDto result) throws IOException, InterruptedException {
        log.info("Running project with context: {}", context);
        return executeCommand(context,
                "run_project.sh",
                context.javaVersion().getPath(),
                context.sourcePath(),
                result.getBuiltJar(),
                context.port().toString(),
                context.javaMinimumMemory(),
                context.javaMaximumMemory(),
                context.environmentVariables());
    }

    /**
     * Handles the successful deployment of an application.
     * Updates the deployment records and logs the success message.
     *
     * @param applicationId the ID of the application that was deployed
     * @param status        the status of the deployment
     * @param jarName       the name of the built JAR file
     * @param pid           the process ID of the running application
     * @param branch        the branch that was deployed
     * @param deploymentType the type of deployment (e.g., ROLLBACK, LATEST)
     * @param finalResult   the final result of the deployment process
     */
    private void handleSuccessfulDeployment(Integer applicationId, String status, String jarName, Integer pid, String branch, DeploymentType deploymentType, List<CommandResultDto> finalResult) {
        if (status.equalsIgnoreCase(DeploymentStatus.SUCCEEDED.name())) {
            log.info("Deployment for application ID {} completed successfully", applicationId);
            updateDeploymentRecords(applicationId, jarName, pid, branch, deploymentType, finalResult);
        } else {
            log.error("Deployment for application ID {} failed",
                    applicationId);
        }
    }

    /**
     * Updates the deployment records in the database.
     * This includes updating the latest deployment to PREVIOUS status and creating a new deployment record.
     *
     * @param applicationId the ID of the application being deployed
     * @param jarName       the name of the built JAR file
     * @param pid           the process ID of the running application
     * @param branch        the branch that was deployed
     * @param deploymentType the type of deployment (e.g., ROLLBACK, LATEST)
     * @param finalResult   the final result of the deployment process
     */
    public void updateDeploymentRecords(Integer applicationId, String jarName, Integer pid, String branch, DeploymentType deploymentType, List<CommandResultDto> finalResult) {
        Deployment latestDeployment = deploymentService.findLatestByApplicationId(applicationId);
        if (latestDeployment != null) {
            if (!deploymentType.equals(DeploymentType.ROLLBACK)) {
                latestDeployment.setType(DeploymentType.PREVIOUS);
            }
            latestDeployment.setStatus(DeploymentStatus.STOPPED);
            deploymentService.update(DeploymentDto.fromEntity(latestDeployment));
        }

        DeploymentDto newDeployment = DeploymentDto.builder()
                .version(jarName)
                .status(DeploymentStatus.RUNNING)
                .type(deploymentType == DeploymentType.ROLLBACK ? DeploymentType.ROLLBACK : DeploymentType.LATEST)            .createdAt(Instant.now())
                .applicationId(applicationId)
                .pid(pid)
                .branch(branch)
                .build();
        DeploymentDto result = deploymentService.save(newDeployment);

        ObjectMapper mapper = new ObjectMapper();
        String finalResultJson;
        try {
            finalResultJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(finalResult);
        } catch (Exception e) {
            log.error("Failed to convert final result to JSON: {}", e.getMessage());
            throw new SpringOpsException("Error converting final result to JSON", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        deploymentService.generateLogsPath(result.getId(), finalResultJson);
    }

    /**
     * Handles the failure of a deployment process.
     * Logs the error and updates the deployment result with failure information.
     *
     * @param applicationId the ID of the application that failed to deploy
     * @param result        the DeploymentResultDto to update with failure information
     * @param e            the exception that caused the failure
     */
    private void handleDeploymentFailure(Integer applicationId, DeploymentResultDto result, Exception e) {
        log.error("Error managing deployment for application ID {}: {}", applicationId, e.getMessage());
        result.setSuccess(false);
        result.setUpdateResult(new CommandResultDto(-1, e.toString(),
                DeploymentStatus.FAILED.name(), "Deployment failed during update step", null, null));
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
    private CommandResultDto executeCommand(DeploymentContextDto context, String scriptName, String... args) throws IOException, InterruptedException {
        log.info("Executing script: {} with {} arguments", scriptName, args.length);
        String scriptPath = Objects.requireNonNull(getClass().getClassLoader().getResource("scripts/" + scriptName)).getPath();

        log.info("Command to be executed: /bin/bash {} {}", scriptPath, String.join(" ", args));

        String command = String.format("/bin/bash %s %s", scriptPath, String.join(" ", args));
        ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", command);
        Process process = processBuilder.start();

        List<String> outputLines;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            outputLines = reader.lines().toList();
        }

        int exitCode = process.waitFor();

        String rawOutput = String.join(System.lineSeparator(), outputLines);
        if (applicationConfig.getDisplayProcessLogs()) {
            log.info("Output from script {}:\n{}", scriptName, rawOutput);
        } else {
            log.debug("Output from script {}:\n{}", scriptName, rawOutput);
        }

        // Find index of the line containing springops-result=
        int startIndex = -1;
        for (int i = 0; i < outputLines.size(); i++) {
            if (outputLines.get(i).startsWith("springops-result=")) {
                startIndex = i;
                break;
            }
        }

        if (startIndex == -1) {
            log.error("Script {} did not return a valid springops-result line.", scriptName);
            return CommandResultDto.builder()
                    .exitCode(exitCode)
                    .output(rawOutput)
                    .status("FAILED")
                    .message("Missing springops-result line in script output")
                    .data(null)
                    .build();
        }

        // Join all lines starting from the springops-result line
        String joined = outputLines.subList(startIndex, outputLines.size()).stream()
                .collect(Collectors.joining(System.lineSeparator()));

        // Extract JSON part
        String json = joined.substring("springops-result=".length()).trim();

        try {
            ObjectMapper mapper = new ObjectMapper();
            log.info("Parsing JSON from springops-result: {}", json);
            CommandResultDto dto = mapper.readValue(json, CommandResultDto.class);
            dto.setDeploymentContext(context);
            return dto;
        } catch (Exception e) {
            log.error("Failed to parse JSON from springops-result: {}", e.getMessage());
            return CommandResultDto.builder()
                    .exitCode(exitCode)
                    .output(json)
                    .status(DeploymentStatus.FAILED.name())
                    .message("Invalid JSON in springops-result")
                    .data(null)
                    .build();
        }
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
    private CommandResultDto captureCommandOutput(DeploymentContextDto context, String scriptName, String... args) throws IOException, InterruptedException {
        return executeCommand(context, scriptName, args);
    }
}
