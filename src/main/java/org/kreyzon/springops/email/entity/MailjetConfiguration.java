package org.kreyzon.springops.email.entity;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.kreyzon.springops.email.enums.EmailProvider;

/**
 * Entity representing the configuration for Mailjet email service.
 * Extends EmailConfiguration to inherit common email configuration properties.
 * @author Domenico Ferraro
 */
@Entity
@Table(name = "mailjet_configuration")
@PrimaryKeyJoinColumn(name = "config_id")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MailjetConfiguration extends EmailConfiguration {

    @Column(name = "api_key", nullable = false)
    private String apiKey;

    @Column(name = "api_secret", nullable = false)
    private String apiSecret;

    @PrePersist
    @PreUpdate
    private void prePersist() {
        setEmailProvider(EmailProvider.MAILJET);
    }
    


}
