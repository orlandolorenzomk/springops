package org.kreyzon.springops.core.email.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.kreyzon.springops.core.email.enums.SmtpSecurity;

/**
 * Entity representing the configuration for SMTP email service.
 * Extends EmailConfiguration to inherit common email configuration properties.
 * @author Domenico Ferraro
 */
@Entity
@Table(name = "smtp_config")
@Getter
@SuperBuilder
@ToString(exclude = {"description", "password"}, callSuper = true)
public class SmtpConfig extends EmailConfiguration {

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

}
