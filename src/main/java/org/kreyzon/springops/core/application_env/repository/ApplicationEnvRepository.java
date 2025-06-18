package org.kreyzon.springops.core.application_env.repository;

import org.kreyzon.springops.core.application.entity.Application;
import org.kreyzon.springops.core.application_env.entity.ApplicationEnv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link ApplicationEnv} entities.
 * This interface extends JpaRepository to provide CRUD operations
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@Repository
public interface ApplicationEnvRepository extends JpaRepository<ApplicationEnv, Integer> {
    /**
     * Finds all ApplicationEnv entities associated with a specific Application.
     *
     * @param application the Application entity
     * @return a list of ApplicationEnv entities
     */
    List<ApplicationEnv> findByApplication(Application application);

    /**
     * Finds an ApplicationEnv entity by its application, name, and value.
     *
     * @param application the Application entity
     * @param name        the name of the environment variable
     * @param value       the value of the environment variable
     * @return an Optional containing the found ApplicationEnv entity, or empty if not found
     */
    Optional<ApplicationEnv> findByApplicationAndNameAndValue(Application application, String name, String value);
}