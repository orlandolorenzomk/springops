package org.kreyzon.springops.common.dto.audits;

import lombok.Value;

/**
 * DTO for representing the status of an audit.
 *
 * @author Lorenzo Orlando
 */
@Value
public class AuditStatusDto {
    String status;
    String description;
}
