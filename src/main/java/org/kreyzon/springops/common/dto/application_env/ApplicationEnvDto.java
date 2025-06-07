package org.kreyzon.springops.common.dto.application_env;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;
import org.kreyzon.springops.core.application_env.entity.ApplicationEnv;

import java.io.Serializable;
import java.time.Instant;

/**
 * DTO for {@link org.kreyzon.springops.core.application_env.entity.ApplicationEnv}
 */
@Value
public class ApplicationEnvDto implements Serializable {
    Integer id;
    @NotNull
    @Size(max = 255)
    String name;
    @NotNull
    String value;
    Instant createdAt;

    Integer applicationId;

    /**
     * Converts an {@link ApplicationEnv} entity to its corresponding DTO.
     *
     * @param applicationEnv the entity to convert
     * @return the converted DTO
     */
    public static ApplicationEnvDto fromEntity(ApplicationEnv applicationEnv) {
        return new ApplicationEnvDto(
                applicationEnv.getId(),
                applicationEnv.getName(),
                applicationEnv.getValue(),
                applicationEnv.getCreatedAt(),
                applicationEnv.getApplication() != null ? applicationEnv.getApplication().getId() : null
        );
    }
}