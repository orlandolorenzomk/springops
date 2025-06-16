package org.kreyzon.springops.audits.repository;

import org.kreyzon.springops.audits.entity.Audit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * Repository interface for managing Audit entities.
 * This interface extends JpaRepository to provide CRUD operations
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@Repository
public interface AuditRepository extends JpaRepository<Audit, Integer>, JpaSpecificationExecutor<Audit> {
    /**
     * Finds all audits older than one month using a native SQL query.
     *
     * @return a list of Audit entities that are older than one month
     */
    @Query(value = "SELECT * FROM audits a WHERE a.timestamp < CURRENT_DATE - INTERVAL '1 month'", nativeQuery = true)
    List<Audit> findOlderThanOneMonthAudits();
}