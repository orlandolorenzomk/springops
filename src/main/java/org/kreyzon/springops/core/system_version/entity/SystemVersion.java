package org.kreyzon.springops.core.system_version.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;

/**
 * Represents a system version entity that stores information about Maven and Java versions.
 * Includes details such as type, version, path, and creation timestamp.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@Getter
@Setter
@Entity
@Table(name = "system_versions")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemVersion {
    /**
     * Unique identifier for the system version record.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    /**
     * The type of the system version (e.g., Maven or Java).
     */
    @Size(max = 20)
    @NotNull
    @Column(name = "type", nullable = false, length = 20)
    private String type;

    /**
     * The version string of the system version.
     */
    @Size(max = 50)
    @NotNull
    @Column(name = "version", nullable = false, length = 50)
    private String version;

    /**
     * The file path or directory associated with the system version.
     */
    @NotNull
    @Column(name = "path", nullable = false, length = Integer.MAX_VALUE)
    private String path;

    /**
     * The timestamp when the system version record was created.
     */
    @Column(name = "created_at")
    private Instant createdAt;

    /**
     * The name of the system version (optional).
     */
    @Column(name = "name", length = 100)
    private String name;
}