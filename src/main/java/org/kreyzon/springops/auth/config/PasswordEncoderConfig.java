package org.kreyzon.springops.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuration class for password encoding.
 * Provides a bean for {@link PasswordEncoder} using {@link BCryptPasswordEncoder}.
 * Ensures secure password hashing for authentication purposes.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * Creates a {@link PasswordEncoder} bean using {@link BCryptPasswordEncoder}.
     *
     * @return a {@link PasswordEncoder} instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}