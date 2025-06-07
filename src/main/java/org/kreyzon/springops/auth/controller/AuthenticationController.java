package org.kreyzon.springops.auth.controller;

import lombok.RequiredArgsConstructor;
import org.kreyzon.springops.auth.service.AuthenticationService;
import org.kreyzon.springops.common.dto.auth.AuthenticationRequestDto;
import org.kreyzon.springops.common.dto.auth.AuthenticationResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for handling user authentication.
 * Provides an endpoint to authenticate users and return a JWT token.
 */
@RestController
@RequestMapping("/authentication")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    /**
     * Authenticates a user and returns an authentication response containing a JWT token.
     *
     * @param authenticationRequestDto the request containing the user's email and password
     * @return a ResponseEntity containing the authentication response with a JWT token, expiration time, user ID, and email
     */
    @PostMapping
    public ResponseEntity<AuthenticationResponseDto> authenticate(@RequestBody AuthenticationRequestDto authenticationRequestDto) {
        return ResponseEntity.ok(authenticationService.authenticate(authenticationRequestDto.getEmail(), authenticationRequestDto.getPassword()));
    }
}