package org.kreyzon.springops.core.email.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.kreyzon.springops.core.email.enums.EmailProvider;
import org.kreyzon.springops.core.email.enums.SmtpSecurity;

import java.time.Instant;
import java.util.UUID;

/**
 * Abstract class representing the base configuration for email services.
 * This class is extended by specific email configuration implementations.
 * Inheritance is managed using the JOINED strategy,
 * @author Domenico Ferraro
 */
@Getter
@Setter
@Entity
@Table(name = "email_configuration")
@Inheritance(strategy = InheritanceType.JOINED)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {"description"})
public abstract class EmailConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "security_protocol", nullable = false)
    @Enumerated(EnumType.STRING)
    private SmtpSecurity securityProtocol;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "use_debug", nullable = false)
    private Boolean useDebug;

    @Column(name = "email_provider", nullable = false)
    @Enumerated(EnumType.STRING)
    private EmailProvider emailProvider;
}

