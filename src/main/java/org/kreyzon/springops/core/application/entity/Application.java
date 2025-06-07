package org.kreyzon.springops.core.application.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.kreyzon.springops.core.system_version.entity.SystemVersion;

import java.time.Instant;

/**
 * Represents an application entity in the system.
 * This entity is used to store information about applications.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@Getter
@Setter
@Entity
@Table(name = "applications")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 255)
    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "folder_root")
    private String folderRoot;

    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;

    @Column(name = "created_at")
    private Instant createdAt;

    @ManyToOne
    @JoinColumn(name = "mvn_system_version_id", referencedColumnName = "id")
    private SystemVersion mvnSystemVersion;

    @ManyToOne
    @JoinColumn(name = "java_system_version_id", referencedColumnName = "id")
    private SystemVersion javaSystemVersion;

    @Column(name = "git_project_https_url")
    private String gitProjectHttpsUrl;
}