package org.kreyzon.springops.core.deployment.repository;

import org.kreyzon.springops.common.enums.DeploymentStatus;
import org.kreyzon.springops.core.application.entity.Application;
import org.kreyzon.springops.core.deployment.entity.Deployment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Deployment entities.
 * This interface extends JpaRepository to provide CRUD operations for Deployment entities.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@Repository
public interface DeploymentRepository extends JpaRepository<Deployment, Integer>, JpaSpecificationExecutor<Deployment> {

    @Query("SELECT d FROM Deployment d WHERE d.application = ?1 AND d.status = ?2")
    List<Deployment> findByApplicationAndStatus(Application application, DeploymentStatus status);

    Optional<Deployment> findByPid(Integer pid);

    @Query("SELECT d FROM Deployment d WHERE d.application.id = ?1 ORDER BY d.createdAt DESC LIMIT 1")
    Optional<Deployment> findByCreatedAtDesc(Integer applicationId);
}