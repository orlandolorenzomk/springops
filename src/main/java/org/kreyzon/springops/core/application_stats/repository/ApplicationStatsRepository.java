package org.kreyzon.springops.core.application_stats.repository;

import org.kreyzon.springops.core.application_stats.entity.ApplicationStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Repository interface for managing application statistics.
 * This interface extends JpaRepository to provide CRUD operations
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@Repository
public interface ApplicationStatsRepository extends JpaRepository<ApplicationStats, Integer> {

    /**
     * Finds application statistics by application ID and timestamp range.
     *
     * @param applicationId the ID of the application
     * @param start the start of the timestamp range
     * @param end the end of the timestamp range
     * @return a list of ApplicationStats entities matching the criteria
     */
    List<ApplicationStats> findByApplicationIdAndTimestampBetween(Integer applicationId, OffsetDateTime start, OffsetDateTime end);

    /**
     * Finds application statistics by cutoff date.
     *
     * @param cutoffDate the date before which statistics should be retrieved
     * @return a list of ApplicationStats entities with timestamps before the cutoff date
     */
    List<ApplicationStats> findByTimestampBefore(OffsetDateTime cutoffDate);
}