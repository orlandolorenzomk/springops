package org.kreyzon.springops.core.system_version.repository;

import org.kreyzon.springops.core.system_version.entity.SystemVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing {@link SystemVersion} entities.
 * Provides CRUD operations and database interaction methods.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@Repository
public interface SystemVersionRepository extends JpaRepository<SystemVersion, Integer> {
}