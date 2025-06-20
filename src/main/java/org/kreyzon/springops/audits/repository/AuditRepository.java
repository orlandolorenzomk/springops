package org.kreyzon.springops.audits.repository;

import org.kreyzon.springops.audits.entity.Audit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
     * Deletes all audits older than the specified number of months.
     *
     * @param months number of months
     */
    @Modifying
    @Query(value = "DELETE FROM audits WHERE timestamp < CURRENT_DATE - CAST(:months || ' months' AS INTERVAL)", nativeQuery = true)
    void deleteOlderThanNMonths(@Param("months") Integer months);


    /**
     * Finds all distinct actions from the Audit table.
     *
     * @return a list of distinct action strings
     */
    @Query("SELECT DISTINCT a.action FROM Audit a")
    List<String> findDistinctActions();

}