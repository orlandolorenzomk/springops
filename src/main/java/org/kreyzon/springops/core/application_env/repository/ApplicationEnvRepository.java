package org.kreyzon.springops.core.application_env.repository;

import org.kreyzon.springops.core.application.entity.Application;
import org.kreyzon.springops.core.application_env.entity.ApplicationEnv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for managing {@link ApplicationEnv} entities.
 * This interface extends JpaRepository to provide CRUD operations
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@Repository
public interface ApplicationEnvRepository extends JpaRepository<ApplicationEnv, Integer> {
    List<ApplicationEnv> findByApplication(Application application);
}