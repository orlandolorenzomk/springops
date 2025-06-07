package org.kreyzon.springops.core.application.repository;

import org.kreyzon.springops.core.application.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

/**
 * Represents the repository interface for managing Application entities.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@Repository
public interface ApplicationRepository extends JpaRepository<Application, Integer> {
    /**
     * Finds an Application by its name.
     *
     * @param name the name of the Application
     * @return the Application if found, null otherwise
     */
    boolean existsByName(String name);
}