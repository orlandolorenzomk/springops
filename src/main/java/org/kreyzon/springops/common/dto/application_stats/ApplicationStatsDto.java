package org.kreyzon.springops.common.dto.application_stats;

import org.kreyzon.springops.core.application_stats.entity.ApplicationStats;
import org.kreyzon.springops.core.application.entity.Application;

import java.time.OffsetDateTime;

/**
 * Represents resource usage statistics for a process at a given time.
 *
 * @param memoryMb    Resident memory usage in megabytes (RSS)
 * @param cpuLoad     CPU usage of the process (0.0–1.0)
 * @param availMemMb  Available system memory in megabytes
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
public record ApplicationStatsDto(
        long memoryMb,
        Double cpuLoad,
        Double availMemMb,

        String timestamp
) {

    /**
     * Build a DTO from a JPA entity.
     *
     * @param entity the ApplicationStats entity
     * @return a DTO containing the data
     */
    public static ApplicationStatsDto fromEntity(ApplicationStats entity) {
        return new ApplicationStatsDto(
                entity.getMemoryMb().longValue(),
                entity.getCpuLoad(),
                entity.getAvailMemMb(),
                entity.getTimestamp().toString()
        );
    }

    /**
     * Convert the DTO into a JPA entity.
     *
     * @param application the owning Application entity
     * @param pid the process ID
     * @param timestamp timestamp of the stats
     * @return a new ApplicationStats entity (unsaved)
     */
    public ApplicationStats toEntity(Application application, Integer pid, OffsetDateTime timestamp) {
        return ApplicationStats.builder()
                .application(application)
                .pid(pid)
                .timestamp(timestamp)
                .memoryMb((double) memoryMb)
                .cpuLoad(cpuLoad)
                .availMemMb(availMemMb)
                .build();
    }
}
