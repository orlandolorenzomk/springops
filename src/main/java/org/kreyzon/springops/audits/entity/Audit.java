package org.kreyzon.springops.audits.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.kreyzon.springops.auth.model.User;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

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


    /**
     * Builds a dynamic specification for searching audits.
     *
     * @param userId the user ID to filter by (optional)
     * @param action the action to filter by (optional)
     * @param from   the start timestamp to filter by (optional)
     * @param to     the end timestamp to filter by (optional)
     * @return a Specification object for querying audits
     */
    public static Specification<Audit> buildSpecification(Integer userId, String action, Instant from, Instant to) {
        return (root, query, criteriaBuilder) -> {
            var predicates = criteriaBuilder.conjunction();

            if (userId != null) {
                predicates.getExpressions().add(criteriaBuilder.equal(root.get("user").get("id"), userId));
            }
            if (action != null && !action.isBlank()) {
                predicates.getExpressions().add(criteriaBuilder.equal(root.get("action"), action));
            }
            if (from != null) {
                predicates.getExpressions().add(criteriaBuilder.greaterThanOrEqualTo(root.get("timestamp"), from));
            }
            if (to != null) {
                predicates.getExpressions().add(criteriaBuilder.lessThanOrEqualTo(root.get("timestamp"), to));
            }

            return predicates;
        };
    }
}