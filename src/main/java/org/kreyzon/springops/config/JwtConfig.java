package org.kreyzon.springops.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration class for JWT properties.
 * Maps the JWT-related properties from the application.yml file.
 * Provides access to the secret key, expiration time, header, and prefix.
 * <p>
 * This class uses Lombok annotations to reduce boilerplate code.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@Component
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtConfig {

    /**
     * Secret key for JWT.
     */
    private String secret;

    /**
     * Expiration time for JWT.
     */
    private int expiration;

    /**
     * Header name for JWT.
     */
    private String header;

    /**
     * Prefix for JWT.
     */
    private String prefix;
}