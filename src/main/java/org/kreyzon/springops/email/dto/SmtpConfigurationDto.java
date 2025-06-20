package org.kreyzon.springops.email.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.kreyzon.springops.email.request.SmtpConfigurationRequest;

import java.time.Instant;

/**
 * Data Transfer Object (DTO) for SMTP email service configuration.
 * This class extends SmtpConfigurationRequest and implements EmailConfigurationDto.
 * It includes fields for the identifier, creation time, and update time,
 * and overrides methods to prevent serialization of sensitive passwords.
 *
 * @author Domenico Ferraro
 */
@SuperBuilder
@Getter
@Jacksonized
public class SmtpConfigurationDto extends SmtpConfigurationRequest implements EmailConfigurationDto {
    private final String id;
    private final Instant createdAt;
    private final Instant updatedAt;

    @Override
    @JsonIgnore
    public String getPassword() {
        return null; // Password should not be serialized
    }


}
