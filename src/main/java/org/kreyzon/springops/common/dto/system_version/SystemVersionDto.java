package org.kreyzon.springops.common.dto.system_version;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;

import java.io.Serializable;
import java.time.Instant;

import org.kreyzon.springops.core.system_version.entity.SystemVersion;

/**
 * Data Transfer Object (DTO) for {@link SystemVersion}.
 * Represents a lightweight version of the entity for data exchange.
 * Includes validation constraints and utility methods for conversion.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@Value
public class SystemVersionDto implements Serializable {
    /**
     * Unique identifier for the system version record.
     */
    Integer id;

    /**
     * The type of the system version (e.g., Maven or Java).
     */
    @NotNull
    @Size(max = 20)
    String type;

    /**
     * The version string of the system version.
     */
    @NotNull
    @Size(max = 50)
    String version;

    /**
     * The file path or directory associated with the system version.
     */
    @NotNull
    String path;

    /**
     * The timestamp when the system version record was created.
     */
    Instant createdAt;

    /**
     * The name of the system version (e.g., "Maven" or "Java").
     * This field is not persisted in the database but is used for display purposes.
     */
    String name;

    /**
     * Converts a {@link SystemVersion} entity to a {@link SystemVersionDto}.
     *
     * @param entity the {@link SystemVersion} entity to convert
     * @return the corresponding {@link SystemVersionDto}
     */
    public static SystemVersionDto fromEntity(SystemVersion entity) {
        return new SystemVersionDto(
            entity.getId(),
            entity.getType(),
            entity.getVersion(),
            entity.getPath(),
            entity.getCreatedAt(),
            entity.getName()
        );
    }

    /**
     * Converts this {@link SystemVersionDto} to a {@link SystemVersion} entity.
     *
     * @return the corresponding {@link SystemVersion} entity
     */
    public static SystemVersion toEntity(SystemVersionDto dto) {
        return SystemVersion.builder()
            .id(dto.getId())
            .type(dto.getType())
            .version(dto.getVersion())
            .path(dto.getPath())
            .createdAt(dto.getCreatedAt())
            .name(dto.getName())
            .build();
    }
}