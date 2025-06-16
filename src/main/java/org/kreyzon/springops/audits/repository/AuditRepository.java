package org.kreyzon.springops.audits.repository;

import org.kreyzon.springops.audits.entity.Audit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing Audit entities.
 * This interface extends JpaRepository to provide CRUD operations
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@Repository
public interface AuditRepository extends JpaRepository<Audit, Integer> {
}