package org.kreyzon.springops.email.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Getter;
import lombok.Setter;
/**
 * Configuration class for email settings.
 * This class is currently empty and can be extended in the future to include email-related configurations.
 *
 * @author Domenico Ferraro
 */
@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "mail.smtp")
public class MailConfig {

    private Integer connectionTimeout;

    private Integer timeout;

    private Integer writeTimeout;

}

