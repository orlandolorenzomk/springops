package org.kreyzon.springops.common.dto.auth;

import lombok.Value;

/**
 * Data Transfer Object for user authentication requests.
 * Contains the user's email and password.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@Value
public class AuthenticationRequestDto {
    String email;
    String password;
}
