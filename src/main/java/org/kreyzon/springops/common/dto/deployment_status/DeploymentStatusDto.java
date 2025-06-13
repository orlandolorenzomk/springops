package org.kreyzon.springops.common.dto.deployment_status;

import lombok.Value;
import org.kreyzon.springops.core.deployment.entity.Deployment;
import org.kreyzon.springops.core.deployment_status.entity.DeploymentStatus;
import org.kreyzon.springops.core.deployment_status.enums.DeploymentStatusEnum;
import org.kreyzon.springops.core.deployment_status.enums.DeploymentStatusType;

import java.time.Instant;
import java.util.UUID;

/**
 * Data Transfer Object (DTO) for Deployment Status.
 * This class is used to transfer deployment status data between layers of the application.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@Value
public class DeploymentStatusDto {
    UUID id;
    Integer deploymentId;
    DeploymentStatusEnum status;
    String message;
    DeploymentStatusType type;
    String logsPath;
    Instant createdAt;

    /**
     * Constructs a DeploymentStatusDto from a DeploymentStatus entity.
     *
     * @param entity the DeploymentStatus entity
     */
    public static DeploymentStatusDto fromEntity(DeploymentStatus entity) {
        return new DeploymentStatusDto(
                entity.getId(),
                entity.getDeployment().getId(),
                entity.getStatus(),
                entity.getMessage(),
                entity.getType(),
                entity.getLogsPath(),
                entity.getCreatedAt()
        );
    }

    /**
     * Converts a DeploymentStatusDto to a DeploymentStatus entity.
     *
     * @param dto the DeploymentStatusDto
     * @param deployment the associated Deployment entity
     * @return a DeploymentStatus entity
     */
    public static DeploymentStatus toEntity(DeploymentStatusDto dto, Deployment deployment) {
        return DeploymentStatus.builder()
                .id(dto.getId())
                .deployment(deployment)
                .status(dto.getStatus())
                .message(dto.getMessage())
                .type(dto.getType())
                .logsPath(dto.getLogsPath())
                .createdAt(dto.getCreatedAt())
                .build();
    }
}
