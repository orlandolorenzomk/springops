package org.kreyzon.springops.common.dto.deployment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;
import org.kreyzon.springops.core.deployment.entity.Deployment;

import java.io.Serializable;
import java.time.Instant;

/**
 * DTO for {@link org.kreyzon.springops.core.deployment.entity.Deployment}
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@Value
@Builder
public class DeploymentDto implements Serializable {
    Integer id;
    @NotNull
    @Size(max = 50)
    String version;
    @NotNull
    @Size(max = 50)
    String status;
    Integer pid;
    @Size(max = 20)
    String type;
    Instant createdAt;
    Integer applicationId;
    String branch;

    /**
     * Constructs a DeploymentDto from a Deployment entity.
     *
     * @param deployment the Deployment entity to convert
     * @return a DeploymentDto representing the Deployment entity
     */
    public static DeploymentDto fromEntity(Deployment deployment) {
        return new DeploymentDto(
                deployment.getId(),
                deployment.getVersion(),
                deployment.getStatus(),
                deployment.getPid(),
                deployment.getType(),
                deployment.getCreatedAt(),
                deployment.getApplication().getId(),
                deployment.getBranch()
        );
    }

    /**
     * Constructs a DeploymentDto from a Deployment entity and an application ID.
     *
     * @param deployment the Deployment entity to convert
     * @return a DeploymentDto representing the Deployment entity with the specified application ID
     */
    public static Deployment toEntity(DeploymentDto deployment) {
        return Deployment.builder()
                .id(deployment.getId())
                .version(deployment.getVersion())
                .status(deployment.getStatus())
                .pid(deployment.getPid())
                .type(deployment.getType())
                .createdAt(deployment.getCreatedAt())
                .branch(deployment.getBranch())
                .build();
    }
}