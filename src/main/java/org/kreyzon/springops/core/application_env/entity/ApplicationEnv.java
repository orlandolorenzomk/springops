package org.kreyzon.springops.core.application_env.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.kreyzon.springops.core.application.entity.Application;

import java.time.Instant;

/**
 * Represents an environment variable for an application.
 * This entity is used to store key-value pairs that are associated with a specific application.
 * Each value is encrypted and stored securely.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@Getter
@Setter
@Entity
@Table(name = "application_env")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationEnv {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Size(max = 255)
    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Column(name = "value", nullable = false, length = Integer.MAX_VALUE)
    private String value;

    @Column(name = "created_at")
    private Instant createdAt;
}