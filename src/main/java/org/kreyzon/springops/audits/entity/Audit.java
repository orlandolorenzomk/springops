package org.kreyzon.springops.audits.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.kreyzon.springops.auth.model.User;

import java.time.Instant;
import java.util.Map;

/**
 * Represents an audit record in the system.
 * This entity captures user actions along with their details and timestamps.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@Getter
@Setter
@Entity
@Table(name = "audits")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Audit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Size(max = 255)
    @NotNull
    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "\"timestamp\"")
    private Instant timestamp;

    @Column(name = "details")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> details;
}