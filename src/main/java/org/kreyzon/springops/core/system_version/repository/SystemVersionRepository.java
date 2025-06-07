package org.kreyzon.springops.core.system_version.repository;

import org.kreyzon.springops.core.system_version.entity.SystemVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for managing {@link SystemVersion} entities.
 * Provides CRUD operations and database interaction methods.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@Repository
public interface SystemVersionRepository extends JpaRepository<SystemVersion, Integer> {

    /**
     * Checks if a system version with the given name exists.
     *
     * @param name the name of the system version to check
     * @return true if a system version with the given name exists, false otherwise
     */
    boolean existsByName(String name);

    /**
     * Finds a system version by its type.
     *
     * @param type the type of the system version to find (e.g., "Maven", "Java")
     * @return an Optional containing the found SystemVersion, or empty if not found
     */
    Optional<SystemVersion> findByType(String type);
}