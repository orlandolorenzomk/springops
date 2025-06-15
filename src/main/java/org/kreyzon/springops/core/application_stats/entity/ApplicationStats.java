package org.kreyzon.springops.core.application_stats.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.kreyzon.springops.core.application.entity.Application;

import java.time.OffsetDateTime;

/**
 * Entity representing statistics for an application process.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@Getter
@Setter
@Entity
@Table(name = "application_stats")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "pid", nullable = false)
    private Integer pid;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @NotNull
    @Column(name = "\"timestamp\"", nullable = false)
    private OffsetDateTime timestamp;

    @NotNull
    @Column(name = "memory_mb", nullable = false)
    private Double memoryMb;

    @NotNull
    @Column(name = "cpu_load", nullable = false)
    private Double cpuLoad;

    @NotNull
    @Column(name = "avail_mem_mb", nullable = false)
    private Double availMemMb;
}