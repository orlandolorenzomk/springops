package org.kreyzon.springops.core.deployment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kreyzon.springops.common.dto.deployment.DeploymentResultDto;
import org.kreyzon.springops.common.dto.deployment.DeploymentStatusDto;
import org.kreyzon.springops.core.deployment.service.DeploymentManagerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/deployment-manager")
@RequiredArgsConstructor
@Slf4j
public class DeploymentManagerController {

    private final DeploymentManagerService deploymentManagerService;

    /**
     * Endpoint to get the current deployment status of an application.
     *
     * @param applicationId the ID of the application to check
     * @return ResponseEntity containing the deployment status
     */
    @GetMapping("/status")
    public ResponseEntity<DeploymentStatusDto> getDeploymentStatus(@RequestParam Integer applicationId) {
        DeploymentStatusDto status = deploymentManagerService.getDeploymentStatus(applicationId);
        return ResponseEntity.ok(status);
    }

    @PostMapping("/kill")
    public ResponseEntity<String> killApplication(@RequestParam Integer pid) {
        boolean killed = deploymentManagerService.killDeploymentProcess(pid);
        if (killed) {
            log.info("Successfully killed process with PID: {}", pid);
            return ResponseEntity.ok("Process killed successfully.");
        } else {
            log.error("Failed to kill process with PID: {}", pid);
            return ResponseEntity.status(500).body("Failed to kill process.");
        }
    }

    /**
     * Endpoint to trigger deployment operations.
     *
     * @param applicationId the ID of the application to deploy
     * @param branchName the branch name to use for the update operation
     * @return ResponseEntity indicating the status of the deployment
     */
    @PostMapping("/deploy")
    public ResponseEntity<DeploymentResultDto> deployApplication(
            @RequestParam Integer applicationId,
            @RequestParam String branchName) {
        DeploymentResultDto result = deploymentManagerService.manageDeployment(applicationId, branchName);
        return ResponseEntity.ok(result);
    }
}