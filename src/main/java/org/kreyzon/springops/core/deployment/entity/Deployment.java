package org.kreyzon.springops.core.deployment.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.kreyzon.springops.common.enums.DeploymentStatus;
import org.kreyzon.springops.common.enums.DeploymentType;
import org.kreyzon.springops.core.application.entity.Application;

import java.time.Instant;


/**
 * Represents a Deployment entity in the system.
 * This entity is used to track deployments of applications, including their version, status, and other relevant details.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@Getter
@Setter
@Entity
@Table(name = "deployments")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Deployment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Size(max = 50)
    @NotNull
    @Column(name = "version", nullable = false, length = 50)
    private String version;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private DeploymentStatus status;

    @Column(name = "pid")
    private Integer pid;

    @Column(name = "type", length = 20)
    @Enumerated(EnumType.STRING)
    private DeploymentType type;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "branch")
    private String branch;

    @Column(name = "logs_path")
    private String logsPath;

    @Column(name = "notes")
    private String notes;

    @Column(name = "time_taken")
    private Integer timeTaken;
}