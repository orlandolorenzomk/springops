package org.kreyzon.springops.common.dto.audits;

import lombok.Value;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for filtering audits.
 *
 * @author Lorenzo Orlando
 */
@Value
public class AuditFilterDto {
    UUID userId;
    String action;
    Instant from;
    Instant to;
}
