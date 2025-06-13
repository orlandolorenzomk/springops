package org.kreyzon.springops.core.deployment_status.repository;

import org.kreyzon.springops.core.deployment_status.entity.DeploymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository interface for managing DeploymentStatus entities.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@Repository
public interface DeploymentStatusRepository extends JpaRepository<DeploymentStatus, UUID> {
}