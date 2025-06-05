package org.kreyzon.springops.setup.repository;

import org.kreyzon.springops.setup.domain.Setup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository interface for managing {@link Setup} entities.
 * Provides CRUD operations and query methods for the Setup entity.
 * Includes a method to check if the setup process is complete.
 * <p>
 * The `isSetupComplete` method uses a native SQL query to determine
 * if the setup process has been completed by checking the `is_complete` column.
 * Returns `false` if no setup row exists or the setup is incomplete.
 * Returns `true` otherwise.
 * </p>
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@Repository
public interface SetupRepository extends JpaRepository<Setup, UUID> {

    /*
     * Retrieves the first setup entry from the database.
     */
    @Query(value = "SELECT * FROM setup ORDER BY created_at LIMIT 1", nativeQuery = true)
    Setup findSetup();
}