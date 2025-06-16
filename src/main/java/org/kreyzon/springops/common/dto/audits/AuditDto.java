package org.kreyzon.springops.common.dto.audits;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;
import org.kreyzon.springops.audits.entity.Audit;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

/**
 * DTO for {@link org.kreyzon.springops.audits.entity.Audit}
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@Value
public class AuditDto implements Serializable {
    Integer id;
    @NotNull
    @Size(max = 255)
    String action;
    Instant timestamp;
    Map<String, Object> details;
    String user;

    /**
     * Converts an Audit entity to an AuditDto.
     *
     * @param audit the Audit entity to convert
     * @return the converted AuditDto
     */
    public static AuditDto fromEntity(Audit audit) {
        return new AuditDto(
                audit.getId(),
                audit.getAction(),
                audit.getTimestamp(),
                audit.getDetails(),
                audit.getUser() != null ? audit.getUser().getUsername() : null
        );
    }

    /**
     * Converts an AuditDto to an Audit entity.
     *
     * @param auditDto the AuditDto to convert
     * @return the converted Audit entity
     */
    public static Audit toEntity(AuditDto auditDto) {
        return Audit.builder()
                .id(auditDto.getId())
                .action(auditDto.getAction())
                .timestamp(auditDto.getTimestamp())
                .details(auditDto.getDetails())
                .build();
    }
}