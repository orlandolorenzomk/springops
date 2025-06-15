package org.kreyzon.springops.common.dto.info;

/**
 * Data Transfer Object (DTO) for application information.
 *
 * @param groupId The group ID of the application.
 * @param artifactId The artifact ID of the application.
 * @param version The version of the application.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
public record InfoDto(String groupId, String artifactId, String version) {}

