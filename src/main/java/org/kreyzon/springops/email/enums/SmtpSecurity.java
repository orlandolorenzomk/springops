package org.kreyzon.springops.email.enums;

import lombok.Getter;
import org.kreyzon.springops.common.exception.SpringOpsException;
import org.springframework.http.HttpStatus;

/**
 * Enum representing the security types for SMTP email service.
 * Provides methods to get the string value and to convert from a string value.
 *
 * @author Domenico Ferraro
 */
@Getter
public enum SmtpSecurity {
    STARTTLS("STARTTLS"),
    NONE("NONE"),
    SSL("SSL"),
    TLS("TLS");

    private final String value;

    SmtpSecurity(String value) {
        this.value = value;
    }

    public static SmtpSecurity fromValue(String value) {
        for (SmtpSecurity security : SmtpSecurity.values()) {
            if (security.value.equalsIgnoreCase(value)) {
                return security;
            }
        }
        throw new SpringOpsException("Unknown SMTP security type: " + value, HttpStatus.BAD_REQUEST);
    }
}
