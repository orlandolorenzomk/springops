package org.kreyzon.springops.core.application.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.kreyzon.springops.core.system_version.entity.SystemVersion;

import java.time.Instant;
import java.util.Set;

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


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "mvn_system_version_id", referencedColumnName = "id")
    private SystemVersion mvnSystemVersion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "java_system_version_id", referencedColumnName = "id")
    private SystemVersion javaSystemVersion;

    @Column(name = "git_project_https_url")
    private String gitProjectHttpsUrl;

    @Column(name = "git_project_ssh_url")
    private String gitProjectSshUrl;

    @Column(name = "port")
    private Integer port;

    @Column(name = "java_minimum_memory")
    private String javaMinimumMemory;

    @Column(name = "java_maximum_memory")
    private String javaMaximumMemory;

    @ManyToMany
    @JoinTable(
            name = "application_dependencies",
            joinColumns = @JoinColumn(name = "application_id"),
            inverseJoinColumns = @JoinColumn(name = "depends_on_application_id")
    )
    private Set<Application> dependencies;
}