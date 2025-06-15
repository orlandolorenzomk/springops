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

    /**
     * Finds all deployments associated with a specific application.
     *
     * @param application the application for which to find deployments
     * @return a list of deployments associated with the specified application
     */
    @Query("SELECT d FROM Deployment d WHERE d.application = ?1 AND d.status = ?2")
    List<Deployment> findByApplicationAndStatus(Application application, DeploymentStatus status);

    /**
     * Finds a deployment by its process ID (pid).
     *
     * @param pid the process ID of the deployment to find
     * @return an Optional containing the found deployment, or empty if no deployment with the given pid exists
     */
    Optional<Deployment> findByPid(Integer pid);

    /**
     * Finds the most recent deployment for a specific application, ordered by creation date in descending order.
     *
     * @param applicationId the ID of the application for which to find the most recent deployment
     * @return an Optional containing the most recent deployment, or empty if no deployments exist for the application
     */
    @Query("SELECT d FROM Deployment d WHERE d.application.id = ?1 ORDER BY d.createdAt DESC LIMIT 1")
    Optional<Deployment> findByCreatedAtDesc(Integer applicationId);

    /**
     * Finds all deployments with a specific status.
     *
     * @param deploymentStatus the status of the deployments to find
     * @return a list of deployments with the specified status
     */
    @Query("SELECT d FROM Deployment d WHERE d.status = ?1")
    List<Deployment> findByStatus(DeploymentStatus deploymentStatus);
}