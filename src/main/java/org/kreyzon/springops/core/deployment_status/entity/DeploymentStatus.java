package org.kreyzon.springops.core.deployment_status.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.kreyzon.springops.core.deployment.entity.Deployment;
import org.kreyzon.springops.core.deployment_status.enums.DeploymentStatusEnum;
import org.kreyzon.springops.core.deployment_status.enums.DeploymentStatusType;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents the status of a deployment in the system.
 * This entity captures the current state of a deployment,
 * including its status, message, type, logs path, and creation timestamp.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@Getter
@Setter
@Entity
@Table(name = "deployment_status")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeploymentStatus {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "deployment_id", nullable = false)
    private Deployment deployment;

    @Size(max = 20)
    @NotNull
    @Column(name = "status", nullable = false, length = 20)
    private DeploymentStatusEnum status;

    @Column(name = "message", length = Integer.MAX_VALUE)
    private String message;

    @Size(max = 20)
    @NotNull
    @Column(name = "type", nullable = false, length = 20)
    private DeploymentStatusType type;

    @Column(name = "logs_path", length = Integer.MAX_VALUE)
    private String logsPath;

    @Column(name = "created_at")
    private Instant createdAt;
}
