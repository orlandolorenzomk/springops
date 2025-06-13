package org.kreyzon.springops.core.deployment_status.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kreyzon.springops.common.dto.deployment_status.DeploymentStatusDto;
import org.kreyzon.springops.core.deployment.entity.Deployment;
import org.kreyzon.springops.core.deployment.service.DeploymentLookupService;
import org.kreyzon.springops.core.deployment_status.repository.DeploymentStatusRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeploymentStatusService {
    private final DeploymentStatusRepository deploymentStatusRepository;

    private final DeploymentLookupService deploymentLookupService;

    /**
     * Saves a list of DeploymentStatusDto objects to the database.
     *
     * @param deploymentStatusDtos the list of DeploymentStatusDto objects to save
     * @return a list of saved DeploymentStatusDto objects
     */
    public List<DeploymentStatusDto> saveAll(List<DeploymentStatusDto> deploymentStatusDtos) {
        log.info("Saving {} deployment statuses", deploymentStatusDtos.size());

        return deploymentStatusDtos.stream()
                .map(dto -> {
                    Deployment deployment = deploymentLookupService.findEntityById(dto.getDeploymentId());
                    var entity = DeploymentStatusDto.toEntity(dto, deployment);
                    return DeploymentStatusDto.fromEntity(deploymentStatusRepository.save(entity));
                })
                .toList();
    }
}
