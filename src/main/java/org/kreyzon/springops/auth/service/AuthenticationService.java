package org.kreyzon.springops.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kreyzon.springops.auth.model.User;
import org.kreyzon.springops.auth.util.JwtUtil;
import org.kreyzon.springops.common.dto.auth.AuthenticationResponseDto;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service to handle user authentication and JWT token generation.
 *
 * <p>Uses {@link UserService} to load user details by email and validate credentials.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * Authenticates the user and generates a JWT token.
     *
     * @param email    the email of the user.
     * @param password the password of the user.
     * @return a JWT token if authentication is successful.
     * @throws IllegalArgumentException if authentication fails.
     */
    public AuthenticationResponseDto authenticate(String email, String password) {
        User user = userService.findByEmail(email);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        log.info("User with email {} authenticated successfully", user.getEmail());

        String token = jwtUtil.generateToken(user.getEmail());
        return AuthenticationResponseDto.builder()
                .token(token)
                .expiration(jwtUtil.getExpiration())
                .userId(user.getId().toString())
                .email(user.getEmail())
                .build();
    }
}