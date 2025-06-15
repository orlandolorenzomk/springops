package org.kreyzon.springops.core.email.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

/**
 * Represents a request for configuring Mailjet email settings.
 * This class extends EmailConfigurationRequest and includes specific fields for Mailjet API credentials.
 * It uses Lombok annotations for boilerplate code reduction and Jackson annotations for JSON serialization.
 *
 * @author Domenico Ferraro
 */
@Getter
@SuperBuilder
@Jacksonized
@ToString(exclude = {"apiKey", "apiSecret"})
public class MailjetConfigurationRequest extends EmailConfigurationRequest {

    @NotBlank(message = "API Key cannot be blank")
    private final String apiKey;

    @NotBlank(message = "API Secret cannot be blank")
    private final String apiSecret;

}
