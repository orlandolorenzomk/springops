package org.kreyzon.springops.email.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.kreyzon.springops.email.enums.SmtpSecurity;

/**
 * Abstract base class for email configuration requests.
 * This class is used to define common properties for different email configurations.
 * It uses Jackson annotations for polymorphic deserialization.
 * @author Domenico Ferraro
 */
@Getter
@SuperBuilder
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "provider"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = SmtpConfigurationRequest.class, name = "SMTP"),
        @JsonSubTypes.Type(value = MailjetConfigurationRequest.class, name = "MAILJET")
})
public abstract class EmailConfigurationRequest {

    @NotBlank(message = "Name cannot be blank")
    private final String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private final String description;

    @Builder.Default
    private final SmtpSecurity securityProtocol = SmtpSecurity.NONE;

    @Builder.Default
    private final Boolean useDebug = false;

}
