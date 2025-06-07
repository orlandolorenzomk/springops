package org.kreyzon.springops.common.dto.auth;

import lombok.Builder;
import lombok.Value;

/**
 * Data Transfer Object for user authentication responses.
 * Contains the user's token, expiration time, user ID, and email.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@Value
@Builder
public class AuthenticationResponseDto {
    String token;
    String expiration;
    String userId;
    String email;
}
