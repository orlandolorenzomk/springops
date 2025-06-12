package org.kreyzon.springops.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration class for application-specific properties.
 * Maps the properties defined under the "application" section in the application.yml file.
 * Provides access to the secret, git token, and standard admin details.
 * <p>
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
     * Algorithm used for encryption or hashing.
     */
    private String algorithm;

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

    /**
     * Name of the root directory where application files are stored.
     */
    private String rootDirectoryName;

    /**
     * Subdirectory within the root directory where application data is stored.
     */
    private String directoryApplications;

    /**
     * Subdirectory within the root directory where source files are stored.
     */
    private String directorySource;

    /**
     * Subdirectory within the root directory where backup files are stored.
     */
    private String directoryBackups;

    /**
     * Maximum number of environment files allowed per application.
     */
    private Integer maximumEnvFilesPerApplication;

    /**
     * Flag to indicate whether to display process logs.
     */
    private Boolean displayProcessLogs;

    /**
     * Flag to indicate whether to display exception stack traces.
     */
    private Boolean displayExceptionStackTraces;

    /**
     * Directory where application logs are stored.
     */
    private String directoryApplicationLogs;
}