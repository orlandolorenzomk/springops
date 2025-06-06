package org.kreyzon.springops.setup.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity class representing the `setup` table in the database.
 * This class is used to map the database table to a Java object.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@Getter
@Setter
@Entity
@Table(name = "setup")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Setup {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "tenant_name")
    private String tenantName;

    @Column(name = "is_setup_complete", nullable = false)
    private Boolean isSetupComplete = false;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "files_root")
    private String filesRoot;

    @Column(name = "is_files_root_initialized", nullable = false)
    private Boolean isFilesRootInitialized;

    @Column(name = "is_first_admin_initialized", nullable = false)
    private Boolean isFirstAdminInitialized;
}