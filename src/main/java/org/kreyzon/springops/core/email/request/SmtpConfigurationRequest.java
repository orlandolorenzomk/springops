package org.kreyzon.springops.core.email.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.hibernate.validator.constraints.Range;

/**
 * Represents a request for configuring SMTP email settings.
 * This class extends EmailConfigurationRequest and includes specific fields for SMTP configuration.
 * It uses Lombok annotations for boilerplate code reduction and Jackson annotations for JSON serialization.
 *
 * @author Domenico Ferraro
 */
@Getter
@SuperBuilder
@Jacksonized
@ToString(exclude = "password")
public class SmtpConfigurationRequest extends EmailConfigurationRequest {

    @NotBlank(message = "Host cannot be blank")
    private final String host;

    @NotNull
    @Range(min = 1, max = 65535, message = "Port must be between 1 and 65535")
    private final Integer port;

    @NotBlank(message = "Protocol cannot be blank")
    private final String protocol;

    @Builder.Default
    private final Boolean useAuth = false;

    private final String username;

    private final String password;

}
