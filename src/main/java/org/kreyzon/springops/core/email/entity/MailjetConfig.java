package org.kreyzon.springops.core.email.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Entity representing the configuration for Mailjet email service.
 * Extends EmailConfiguration to inherit common email configuration properties.
 * @author Domenico Ferraro
 */
@Entity
@Table(name = "mailjet_configuration")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@ToString(callSuper = true, exclude = {"apiKey", "apiSecret"})
public class MailjetConfig extends EmailConfiguration {

    @Column(name = "api_key", nullable = false)
    private String apiKey;

    @Column(name = "api_secret", nullable = false)
    private String apiSecret;

}
