package org.kreyzon.springops.common.dto.auth;

import lombok.Builder;
import lombok.Data;

/**
 * DTO for returning the initialized admin user and the generated password.
 */
@Data
@Builder
public class AdminUserResponseDto {
    private UserDto user;
    private String generatedPassword;
}