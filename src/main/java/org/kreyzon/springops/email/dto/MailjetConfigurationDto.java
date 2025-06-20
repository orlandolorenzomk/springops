package org.kreyzon.springops.email.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.kreyzon.springops.email.request.MailjetConfigurationRequest;

import java.time.Instant;

/**
 * Data Transfer Object (DTO) for Mailjet email service configuration.
 * This class extends MailjetConfigurationRequest and implements EmailConfigurationDto.
 * It includes fields for the identifier, creation time, and update time,
 * and overrides methods to prevent serialization of sensitive API keys.
 *
 * @author Domenico Ferraro
 */
@Getter
@SuperBuilder
@Jacksonized
public class MailjetConfigurationDto extends MailjetConfigurationRequest implements EmailConfigurationDto  {
    private final String id;
    private final Instant createdAt;
    private final Instant updatedAt;

    @Override
    @JsonIgnore
    public String getApiKey() {
        return null; // API Key should not be serialized
    }

    @Override
    @JsonIgnore
    public String getApiSecret() {
        return null; // API Secret should not be serialized
    }


}
