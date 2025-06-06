package org.kreyzon.springops.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration class for application-specific properties.
 * Maps the properties defined under the "application" section in the application.yml file.
 * Provides access to the secret, git token, and standard admin details.
 *
 * This class uses Lombok annotations to reduce boilerplate code.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@Component
@ConfigurationProperties(prefix = "application")
@Getter
@Setter
public class ApplicationConfig {

    /**
     * Secret key for the application.
     */
    private String secret;

    /**
     * Git token for the application.
     */
    private String gitToken;

    /**
     * Standard admin username for the application.
     */
    private String standardAdminUsername;

    /**
     * Standard admin email for the application.
     */
    private String standardAdminEmail;

    /**
     * Standard admin password length for the application.
     */
    private int standardAdminPasswordLength;
}