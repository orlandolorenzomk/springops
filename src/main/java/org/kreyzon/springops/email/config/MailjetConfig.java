package org.kreyzon.springops.email.config;

import lombok.Getter;
import lombok.Setter;
import org.kreyzon.springops.email.enums.SmtpSecurity;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.EnumMap;
import java.util.Map;

/**
 * Configuration properties for Mailjet email settings, loaded from application.yml.
 *
 * @author Domenico Ferraro
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "mailjet")
public class MailjetConfig {

    //Mailjet host name
    private String host;

    //Mailjet protocol
    private String protocol;

    //Map of ports based on the security type
    private Map<SmtpSecurity, Integer> ports = new EnumMap<>(SmtpSecurity.class);

}

