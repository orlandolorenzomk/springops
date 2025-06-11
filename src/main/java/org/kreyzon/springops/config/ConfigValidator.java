package org.kreyzon.springops.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kreyzon.springops.common.exception.SpringOpsException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.util.Base64;
import java.util.List;

/**
 * Component to validate application configuration properties on startup.
 * Ensures that JWT secret, application secret, and algorithm are valid.
 *
 * <p>This class performs validation checks for critical configuration properties
 * such as JWT secret, application secret, and encryption algorithm. If any of
 * these properties are invalid or missing, the application will fail to start.</p>
 *
 * <p>Supported algorithms for encryption include AES, RSA, and HMAC.</p>
 *
 * <p>Additionally, the format of the JWT secret and application secret is validated
 * to ensure they are Base64-encoded and meet the required length.</p>
 *
 * @author Lorenzo Orlando
 * @email  orlandolorenzo@kreyzon.com
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ConfigValidator {

    private final JwtConfig jwtConfig;
    private final ApplicationConfig applicationConfig;

    /**
     * Validates the configuration properties after the application context is initialized.
     * This method is executed automatically on application startup.
     */
    @PostConstruct
    public void validateConfigs() {
        validateJwtSecret();
        validateAppSecret();
        validateAlgorithm();
        log.info("Configuration validation completed successfully.");
    }

    /**
     * Validates the JWT secret to ensure it is not null, not empty, and has a valid format.
     * @throws SpringOpsException if the JWT secret is invalid or missing.
     */
    private void validateJwtSecret() {
        if (jwtConfig.getSecret() == null || jwtConfig.getSecret().isEmpty()) {
            throw new SpringOpsException("JWT secret is invalid or missing.", HttpStatus.BAD_REQUEST);
        }
        validateKeyFormat(jwtConfig.getSecret(), "JWT secret");
        log.info("JWT secret is valid and properly formatted.");
    }

    /**
     * Validates the application secret to ensure it is not null, not empty, and has a valid format.
     * @throws SpringOpsException with {@link HttpStatus#BAD_REQUEST} if the application secret is invalid or missing.
     */
    private void validateAppSecret() {
        if (applicationConfig.getSecret() == null || applicationConfig.getSecret().isEmpty()) {
            throw new SpringOpsException("Application secret is invalid or missing.", HttpStatus.BAD_REQUEST);
        }
        validateKeyFormat(applicationConfig.getSecret(), "Application secret");
        log.info("Application secret is valid and properly formatted.");
    }

    /**
     * Validates the encryption algorithm to ensure it is supported.
     * Supported algorithms include AES, RSA, and HMAC.
     * @throws SpringOpsException with {@link HttpStatus#BAD_REQUEST} if the algorithm is invalid or unsupported.
     */
    private void validateAlgorithm() {
        String algorithm = applicationConfig.getAlgorithm();
        List<String> supportedAlgorithms = List.of("AES", "RSA", "HMAC");

        if (algorithm == null || algorithm.isEmpty() || !supportedAlgorithms.contains(algorithm.toUpperCase())) {
            throw new SpringOpsException("Application algorithm is invalid or unsupported: " + algorithm, HttpStatus.BAD_REQUEST);
        }
        log.info("Application algorithm '{}' is valid and supported.", algorithm);
    }

    /**
     * Validates the format of a secret key to ensure it is Base64-encoded and meets the required length.
     *
     * @param key     the secret key to validate.
     * @param keyName the name of the key (used for error messages).
     * @throws SpringOpsException with {@link HttpStatus#BAD_REQUEST} if the key is not valid.
     */
    private void validateKeyFormat(String key, String keyName) {
        try {
            byte[] decodedKey = Base64.getDecoder().decode(key);
            if (decodedKey.length < 16) { // Example: Minimum 128-bit key length
                throw new SpringOpsException(keyName + " is too short. It must be at least 128 bits.", HttpStatus.BAD_REQUEST);
            }
        } catch (IllegalArgumentException e) {
            throw new SpringOpsException(keyName + " is not a valid Base64-encoded string.", HttpStatus.BAD_REQUEST);
        }
    }
}