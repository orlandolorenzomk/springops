package org.kreyzon.springops.core.email.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.kreyzon.springops.core.email.dto.SmtpConfigurationDto;
import org.kreyzon.springops.core.email.enums.EmailProvider;
import org.kreyzon.springops.core.email.request.SmtpConfigurationRequest;

/**
 * Entity representing the configuration for SMTP email service.
 * Extends EmailConfiguration to inherit common email configuration properties.
 * @author Domenico Ferraro
 */
@Entity
@Table(name = "smtp_config")
@PrimaryKeyJoinColumn(name = "config_id")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SmtpConfiguration extends EmailConfiguration {

    @Column(name = "host", nullable = false)
    private String host;

    @Column(name = "port", nullable = false)
    private Integer port;

    @Column(name = "protocol", nullable = false)
    private String protocol;

    @Column(name = "use_auth", nullable = false)
    private Boolean useAuth;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @PrePersist
    @PreUpdate
    private void prePersist() {
        setEmailProvider(EmailProvider.SMTP);
    }

}
